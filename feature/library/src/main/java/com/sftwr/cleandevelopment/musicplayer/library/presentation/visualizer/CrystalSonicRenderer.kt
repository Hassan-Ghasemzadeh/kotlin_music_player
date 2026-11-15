package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * OpenGL ES 2.0 Renderer for 'CrystalSonic' visualizer.
 * Implements smooth, slow particle travel with larger wireframe triangles and late edge-only alpha attenuation.
 */
class CrystalSonicRenderer : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var programId = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var angleHandle = 0
    private var dnaWeightHandle = 0
    private var strandSignHandle = 0
    private var particleParamsHandle = 0
    private var isParticleHandle = 0
    private var aspectUniformHandle = 0
    private var mvpMatrixHandle = 0
    private var timeHandle = 0
    private var fftValuesHandle = 0
    private var bassHandle = 0
    private var averageFftHandle = 0

    private var dnaVertexBuffer: FloatBuffer? = null
    private var particleVertexBuffer: FloatBuffer? = null

    private var rawFftData: FloatArray = FloatArray(32) { 0.2f }
    private var smoothedFftData: FloatArray = FloatArray(32) { 0.2f }
    private val fftLock = Any()

    private var startTime = SystemClock.elapsedRealtime()
    private var currentAspect = 1.0f

    fun updateFft(newFft: FloatArray) {
        synchronized(fftLock) {
            val count = minOf(newFft.size, rawFftData.size)
            System.arraycopy(newFft, 0, rawFftData, 0, count)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)

        val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec4 vColor;
            attribute float vAngle;
            attribute float vDnaWeight;
            attribute float vStrandSign;
            attribute vec4 vParticleParams; // x: speed, y: timeOffset, z: size, w: rotSpeed
            
            uniform mat4 uMVPMatrix;
            uniform float u_time;
            uniform float u_bassValue;
            uniform float u_averageFft;
            uniform float u_fftValues[32];
            uniform float u_isParticle;
            uniform float u_aspect;
            
            varying vec4 fragColor;
            
            void main() {
                vec4 pos = vec4(0.0, 0.0, 0.0, 1.0);
                float alphaFade = 1.0;
                
                if (u_isParticle > 0.5) {
                    // Dynamic Particle System: Smooth travel with larger triangles and late edge-only fade
                    float speed = vParticleParams.x;
                    float timeOffset = vParticleParams.y;
                    float particleSize = vParticleParams.z;
                    float rotSpeed = vParticleParams.w;
                    
                    float period = 8.5;
                    float age = mod(u_time + timeOffset, period);
                    
                    // Outward radial distance drifting across full viewport space
                    float r = 1.52 + speed * age;
                    
                    // Particle center coordinates in direction vAngle
                    vec2 centerPos = vec2(r * cos(vAngle), r * sin(vAngle));
                    
                    // Rotation
                    float rotAngle = vAngle + age * rotSpeed;
                    float cRot = cos(rotAngle);
                    float sRot = sin(rotAngle);
                    mat2 rotMat = mat2(cRot, sRot, -sRot, cRot);
                    
                    // Scale and rotate unit triangle vertex
                    vec2 localPos = rotMat * (vPosition.xy * particleSize);
                    pos = vec4(centerPos + localPos, 0.0, 1.0);
                    
                    // Screen-Edge Proximity Metric:
                    float aspectFactor = max(u_aspect, 1.0);
                    float uNorm = abs(pos.x) / 1.76;
                    float vNorm = abs(pos.y) / (1.76 * aspectFactor);
                    float edgeDist = sqrt(uNorm * uNorm + vNorm * vNorm);
                    
                    float fadeIn = smoothstep(1.50, 1.56, length(pos.xy));
                    float fadeOut = 1.0 - smoothstep(0.96, 1.08, edgeDist);
                    
                    alphaFade = fadeIn * fadeOut;
                    
                } else {
                    // DNA Ring Geometry
                    float normAngle = vAngle / 6.28318530718;
                    normAngle = fract(normAngle);
                    if (normAngle < 0.0) normAngle += 1.0;
                    
                    int fftIdx = int(floor(normAngle * 32.0));
                    if (fftIdx < 0) fftIdx = 0;
                    if (fftIdx > 31) fftIdx = 31;
                    
                    float bandFft = u_fftValues[fftIdx];
                    float dnaFactor = clamp(vDnaWeight, 0.0, 1.0);
                    
                    float audioEnergy = bandFft * dnaFactor + u_bassValue * 0.4 * dnaFactor;
                    float activeAudio = smoothstep(0.08, 0.22, audioEnergy);
                    
                    float dynamicGap = activeAudio * dnaFactor * (0.0375 + bandFft * 0.1125 + u_bassValue * 0.060);
                    float helixDisplacement = sin(vAngle * 14.0) * dynamicGap * vStrandSign;
                    
                    float r = 1.56 + helixDisplacement;
                    pos = vec4(r * cos(vAngle), r * sin(vAngle), 0.0, 1.0);
                }
                
                gl_Position = uMVPMatrix * pos;
                
                vec4 col = vColor;
                col.a *= alphaFade;
                col.rgb *= (0.85 + u_bassValue * 0.45 + u_averageFft * 0.3);
                fragColor = col;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 fragColor;
            
            void main() {
                gl_FragColor = fragColor;
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        setupDnaLadderGeometry()
        setupParticleGeometry()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(projectionMatrix, 0)

        currentAspect = if ((width > 0) && (height > 0)) height.toFloat() / width.toFloat() else 1.0f

        val minDim = minOf(width, height).toFloat()
        val baseSpan = 1.8f
        val xSpan = baseSpan * (width.toFloat() / minDim)
        val ySpan = baseSpan * (height.toFloat() / minDim)

        Matrix.orthoM(projectionMatrix, 0, -xSpan, xSpan, -ySpan, ySpan, -1f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)

        val currentTime = SystemClock.elapsedRealtime()
        val timeSec = (currentTime - startTime) / 1000f

        synchronized(fftLock) {
            for (i in smoothedFftData.indices) {
                val target = rawFftData[i]
                smoothedFftData[i] = (smoothedFftData[i] * 0.72f) + (target * 0.28f)
            }
        }

        var bassSum = 0f
        for (i in 0 until 5) {
            bassSum += smoothedFftData[i]
        }
        val bassValue = bassSum / 5f
        val averageFft = smoothedFftData.average().toFloat()

        GLES20.glUseProgram(programId)

        positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(programId, "vColor")
        angleHandle = GLES20.glGetAttribLocation(programId, "vAngle")
        dnaWeightHandle = GLES20.glGetAttribLocation(programId, "vDnaWeight")
        strandSignHandle = GLES20.glGetAttribLocation(programId, "vStrandSign")
        particleParamsHandle = GLES20.glGetAttribLocation(programId, "vParticleParams")

        isParticleHandle = GLES20.glGetUniformLocation(programId, "u_isParticle")
        aspectUniformHandle = GLES20.glGetUniformLocation(programId, "u_aspect")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        timeHandle = GLES20.glGetUniformLocation(programId, "u_time")
        fftValuesHandle = GLES20.glGetUniformLocation(programId, "u_fftValues")
        bassHandle = GLES20.glGetUniformLocation(programId, "u_bassValue")
        averageFftHandle = GLES20.glGetUniformLocation(programId, "u_averageFft")

        GLES20.glUniform1f(timeHandle, timeSec)
        if (aspectUniformHandle >= 0) GLES20.glUniform1f(aspectUniformHandle, currentAspect)
        if (bassHandle >= 0) GLES20.glUniform1f(bassHandle, bassValue)
        if (averageFftHandle >= 0) GLES20.glUniform1f(averageFftHandle, averageFft)
        if (fftValuesHandle >= 0) GLES20.glUniform1fv(fftValuesHandle, 32, smoothedFftData, 0)

        GLES20.glLineWidth(3.5f)
        val stride = 14 * 4

        fun bindAttributes(buffer: FloatBuffer) {
            buffer.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, buffer)

            buffer.position(3)
            GLES20.glEnableVertexAttribArray(colorHandle)
            GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, stride, buffer)

            buffer.position(7)
            GLES20.glEnableVertexAttribArray(angleHandle)
            GLES20.glVertexAttribPointer(angleHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)

            if (dnaWeightHandle >= 0) {
                buffer.position(8)
                GLES20.glEnableVertexAttribArray(dnaWeightHandle)
                GLES20.glVertexAttribPointer(dnaWeightHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)
            }

            if (strandSignHandle >= 0) {
                buffer.position(9)
                GLES20.glEnableVertexAttribArray(strandSignHandle)
                GLES20.glVertexAttribPointer(strandSignHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)
            }

            if (particleParamsHandle >= 0) {
                buffer.position(10)
                GLES20.glEnableVertexAttribArray(particleParamsHandle)
                GLES20.glVertexAttribPointer(particleParamsHandle, 4, GLES20.GL_FLOAT, false, stride, buffer)
            }
        }

        // 1. Draw dynamic dual-strand DNA ladder frequency ring
        if (isParticleHandle >= 0) GLES20.glUniform1f(isParticleHandle, 0.0f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -timeSec * 8f, 0f, 0f, 1f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        dnaVertexBuffer?.let { buffer ->
            bindAttributes(buffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, dnaVertexCount)
        }

        // 2. Draw dynamic emitting wireframe particle field
        if (isParticleHandle >= 0) GLES20.glUniform1f(isParticleHandle, 1.0f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        particleVertexBuffer?.let { buffer ->
            bindAttributes(buffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, particleVertexCount)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisableVertexAttribArray(angleHandle)
        if (dnaWeightHandle >= 0) GLES20.glDisableVertexAttribArray(dnaWeightHandle)
        if (strandSignHandle >= 0) GLES20.glDisableVertexAttribArray(strandSignHandle)
        if (particleParamsHandle >= 0) GLES20.glDisableVertexAttribArray(particleParamsHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private var dnaVertexCount = 0

    private fun setupDnaLadderGeometry() {
        val segments = 96
        val coords = FloatArray(segments * 3 * 2 * 14)
        var index = 0

        for (i in 0 until segments) {
            val angle1 = ((2.0 * PI * i.toDouble()) / segments.toDouble()).toFloat()
            val angle2 = ((2.0 * PI * (i + 1).toDouble()) / segments.toDouble()).toFloat()

            val w1 = calculateDnaWeight(angle1)
            val w2 = calculateDnaWeight(angle2)

            val t = i.toFloat() / segments
            val (red, green, blue) = getGradientColor(t)
            val alpha = 0.92f

            fun addDnaVertex(angle: Float, weight: Float, strandSign: Float) {
                coords[index++] = 0f; coords[index++] = 0f; coords[index++] = 0f
                coords[index++] = red; coords[index++] = green; coords[index++] = blue; coords[index++] = alpha
                coords[index++] = angle
                coords[index++] = weight
                coords[index++] = strandSign
                coords[index++] = 0f; coords[index++] = 0f; coords[index++] = 0f; coords[index++] = 0f
            }

            addDnaVertex(angle1, w1, +1.0f)
            addDnaVertex(angle2, w2, +1.0f)

            addDnaVertex(angle1, w1, -1.0f)
            addDnaVertex(angle2, w2, -1.0f)

            if ((i % 2 == 0) && (w1 > 0.05f)) {
                addDnaVertex(angle1, w1, +1.0f)
                addDnaVertex(angle1, w1, -1.0f)
            }
        }

        dnaVertexCount = index / 14
        dnaVertexBuffer = ByteBuffer.allocateDirect(index * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(coords, 0, index)
            position(0)
        }
    }

    private fun calculateDnaWeight(angle: Float): Float {
        val center1 = ((PI) / 3.0).toFloat()
        val center2 = ((4.0 * PI) / 3.0).toFloat()

        val w1 = computeArcWeight(angle, center1)
        val w2 = computeArcWeight(angle, center2)

        return (w1 + w2).coerceIn(0f, 1f)
    }

    private fun computeArcWeight(angle: Float, center: Float): Float {
        val halfWidth = (PI / 3.0).toFloat()
        var diff = angle - center
        while (diff > PI) diff -= (2.0 * PI).toFloat()
        while (diff < -PI) diff += (2.0 * PI).toFloat()

        val absDiff = kotlin.math.abs(diff)
        if (absDiff >= halfWidth) return 0f

        val t = absDiff / halfWidth
        val cosVal = cos((PI * t.toDouble()) / 2.0).toFloat()
        return cosVal * cosVal
    }

    private var particleVertexCount = 0

    private fun setupParticleGeometry() {
        val particles = 28
        val coords = FloatArray(particles * 6 * 14)
        var index = 0

        val v1x = 0.0f; val v1y = 1.0f
        val v2x = -0.866025f; val v2y = -0.5f
        val v3x = 0.866025f; val v3y = -0.5f

        for (i in 0 until particles) {
            val angleJitter = sin(i.toDouble() * 2.3).toFloat() * 0.25f
            val angle = (i.toFloat() * ((2f * PI.toFloat()) / particles.toFloat())) + angleJitter

            val speed = 0.08f + (((1f + sin(i.toDouble() * 3.7).toFloat()) * 0.5f) * 0.10f)
            val timeOffset = ((1f + cos(i.toDouble() * 1.9).toFloat()) * 0.5f) * 8.5f

            // افزایش سایز ذرات برای تولید مثلث‌های بزرگ‌تر و برجسته‌تر (0.045 تا 0.095)
            val size = 0.045f + (((1f + sin(i.toDouble() * 4.1).toFloat()) * 0.5f) * 0.050f)

            val rotSpeed = -0.3f + (((1f + cos(i.toDouble() * 2.7).toFloat()) * 0.5f) * 0.6f)

            val t = i.toFloat() / particles
            val (red, green, blue) = getGradientColor(t)
            val alphaScale = (1f + sin(i.toDouble() * 2.9).toFloat()) * 0.5f
            val alpha = 0.45f + (alphaScale * 0.35f)

            val dnaWeight = 0.0f
            val strandSign = 0.0f

            fun addParticleVertex(lx: Float, ly: Float) {
                coords[index++] = lx; coords[index++] = ly; coords[index++] = 0f
                coords[index++] = red; coords[index++] = green; coords[index++] = blue; coords[index++] = alpha
                coords[index++] = angle
                coords[index++] = dnaWeight
                coords[index++] = strandSign
                coords[index++] = speed; coords[index++] = timeOffset; coords[index++] = size; coords[index++] = rotSpeed
            }

            addParticleVertex(v1x, v1y)
            addParticleVertex(v2x, v2y)

            addParticleVertex(v2x, v2y)
            addParticleVertex(v3x, v3y)

            addParticleVertex(v3x, v3y)
            addParticleVertex(v1x, v1y)
        }

        particleVertexCount = index / 14
        particleVertexBuffer = ByteBuffer.allocateDirect(index * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(coords, 0, index)
            position(0)
        }
    }

    private fun getGradientColor(t: Float): Triple<Float, Float, Float> {
        val normalizedT = ((t % 1.0f) + 1.0f) % 1.0f
        return when {
            normalizedT < 0.33f -> {
                val factor = normalizedT / 0.33f
                Triple(1.0f, 0.75f - (factor * 0.35f), 0.15f)
            }
            normalizedT < 0.66f -> {
                val factor = (normalizedT - 0.33f) / 0.33f
                Triple(1.0f - (factor * 0.4f), 0.4f - (factor * 0.2f), 0.15f + (factor * 0.75f))
            }
            else -> {
                val factor = (normalizedT - 0.66f) / 0.34f
                Triple(0.6f + (factor * 0.4f), 0.2f + (factor * 0.55f), 0.9f - (factor * 0.75f))
            }
        }
    }
}