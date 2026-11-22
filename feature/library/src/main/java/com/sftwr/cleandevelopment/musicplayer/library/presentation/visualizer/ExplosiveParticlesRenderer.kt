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

class ExplosiveParticlesRenderer : GLSurfaceView.Renderer {

    @Volatile
    var discRatio: Float = 0.82f

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var programId = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var angleHandle = 0
    private var elementTypeHandle = 0
    private var particleParamsHandle = 0
    private var discRatioHandle = 0

    private var mvpMatrixHandle = 0
    private var timeHandle = 0
    private var fftValuesHandle = 0
    private var bassHandle = 0
    private var averageFftHandle = 0

    private var ringVertexBuffer: FloatBuffer? = null
    private var ringVertexCount = 0

    private var dotVertexBuffer: FloatBuffer? = null
    private var dotVertexCount = 0

    private var tailVertexBuffer: FloatBuffer? = null
    private var tailVertexCount = 0

    private var cometVertexBuffer: FloatBuffer? = null
    private var cometVertexCount = 0

    private var rawFftData: FloatArray = FloatArray(32) { 0.2f }
    private var smoothedFftData: FloatArray = FloatArray(32) { 0.2f }
    private val fftLock = Any()

    private var startTime = SystemClock.elapsedRealtime()

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
            attribute float vElementType; // 0.0 = base ring, 1.0 = pulsing dot, 1.5 = trailing tail, 2.0 = comet
            attribute vec2 vParticleParams; // x: speed, y: timeOffset
            
            uniform mat4 uMVPMatrix;
            uniform float u_time;
            uniform float u_bassValue;
            uniform float u_fftValues[32];
            uniform float u_discRatio;
            
            varying vec4 fragColor;
            
            void main() {
                vec4 pos = vec4(0.0, 0.0, 0.0, 1.0);
                float alphaMod = 1.0;
                
                float normAngle = vAngle / 6.28318530718;
                normAngle = fract(normAngle);
                if (normAngle < 0.0) normAngle += 1.0;
                
                int fftIdx = int(floor(normAngle * 32.0));
                if (fftIdx < 0) fftIdx = 0;
                if (fftIdx > 31) fftIdx = 31;
                
                float bandFft = u_fftValues[fftIdx];
                
                // Dynamically calculate inner base radius in world space (baseSpan = 1.2)
                float baseRadius = (u_discRatio + 0.025) * 1.2;
                float rDot = baseRadius + 0.04 + (bandFft * 0.12) + (u_bassValue * 0.06);
                
                if (vElementType < 0.5) {
                    // 1. Base Continuous Circle Ring
                    pos = vec4(baseRadius * cos(vAngle), baseRadius * sin(vAngle), 0.0, 1.0);
                    alphaMod = 0.90;
                    gl_PointSize = 3.0;
                    
                } else if (vElementType < 1.2) {
                    // 2. Pulsing Dots Ring
                    pos = vec4(rDot * cos(vAngle), rDot * sin(vAngle), 0.0, 1.0);
                    alphaMod = 1.0;
                    gl_PointSize = 7.5;
                    
                } else if (vElementType < 1.7) {
                    // 3. Trailing Tails
                    float tParam = vPosition.x;
                    float rVertex = mix(baseRadius, rDot, tParam);
                    
                    pos = vec4(rVertex * cos(vAngle), rVertex * sin(vAngle), 0.0, 1.0);
                    alphaMod = smoothstep(0.0, 0.6, tParam) * 0.65;
                    gl_PointSize = 3.0;
                    
                } else {
                    // 4. Explosive Comets
                    float speed = vParticleParams.x;
                    float timeOffset = vParticleParams.y;
                    float period = 3.0;
                    float age = mod((u_time * (1.0 + u_bassValue * 0.6)) + timeOffset, period);
                    
                    float rHead = baseRadius + (speed * age) + (u_bassValue * 0.2 * age);
                    float tParam = vPosition.x;
                    float rVertex = rHead - (tParam * 0.06);
                    
                    pos = vec4(rVertex * cos(vAngle), rVertex * sin(vAngle), 0.0, 1.0);
                    float currentRadius = length(pos.xy);
                    alphaMod = tParam * smoothstep(baseRadius + 0.01, baseRadius + 0.11, currentRadius) * (1.0 - smoothstep(1.30, 2.00, currentRadius));
                    gl_PointSize = 4.0;
                }
                
                gl_Position = uMVPMatrix * pos;
                
                vec4 col = vColor;
                col.a *= alphaMod;
                col.rgb *= (0.85 + (bandFft * 0.45) + (u_bassValue * 0.40));
                fragColor = col;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 fragColor;
            
            void main() {
                vec2 coord = gl_PointCoord - vec2(0.5);
                if (length(coord) > 0.5) {
                    discard;
                }
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

        setupGeometry()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(projectionMatrix, 0)

        val minDim = minOf(width, height).toFloat().coerceAtLeast(1f)
        val baseSpan = 1.2f
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
                smoothedFftData[i] = (smoothedFftData[i] * 0.70f) + (target * 0.30f)
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
        elementTypeHandle = GLES20.glGetAttribLocation(programId, "vElementType")
        particleParamsHandle = GLES20.glGetAttribLocation(programId, "vParticleParams")

        discRatioHandle = GLES20.glGetUniformLocation(programId, "u_discRatio")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        timeHandle = GLES20.glGetUniformLocation(programId, "u_time")
        fftValuesHandle = GLES20.glGetUniformLocation(programId, "u_fftValues")
        bassHandle = GLES20.glGetUniformLocation(programId, "u_bassValue")
        averageFftHandle = GLES20.glGetUniformLocation(programId, "u_averageFft")

        GLES20.glUniform1f(timeHandle, timeSec)
        if (discRatioHandle >= 0) GLES20.glUniform1f(discRatioHandle, discRatio)
        if (bassHandle >= 0) GLES20.glUniform1f(bassHandle, bassValue)
        if (averageFftHandle >= 0) GLES20.glUniform1f(averageFftHandle, averageFft)
        if (fftValuesHandle >= 0) GLES20.glUniform1fv(fftValuesHandle, 32, smoothedFftData, 0)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -timeSec * 7f, 0f, 0f, 1f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val stride = 11 * 4

        fun drawBuffer(buffer: FloatBuffer?, count: Int, mode: Int, lineWidth: Float) {
            buffer?.let {
                it.position(0)
                GLES20.glEnableVertexAttribArray(positionHandle)
                GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, it)

                it.position(3)
                GLES20.glEnableVertexAttribArray(colorHandle)
                GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, stride, it)

                it.position(7)
                GLES20.glEnableVertexAttribArray(angleHandle)
                GLES20.glVertexAttribPointer(angleHandle, 1, GLES20.GL_FLOAT, false, stride, it)

                if (elementTypeHandle >= 0) {
                    it.position(8)
                    GLES20.glEnableVertexAttribArray(elementTypeHandle)
                    GLES20.glVertexAttribPointer(elementTypeHandle, 1, GLES20.GL_FLOAT, false, stride, it)
                }

                if (particleParamsHandle >= 0) {
                    it.position(9)
                    GLES20.glEnableVertexAttribArray(particleParamsHandle)
                    GLES20.glVertexAttribPointer(particleParamsHandle, 2, GLES20.GL_FLOAT, false, stride, it)
                }

                GLES20.glLineWidth(lineWidth)
                GLES20.glDrawArrays(mode, 0, count)
            }
        }

        // 1. رسم خط پایه دایره‌ای
        drawBuffer(ringVertexBuffer, ringVertexCount, GLES20.GL_LINES, 2.0f)

        // 2. رسم دنباله‌های عمودی زیر دایره‌ها با ضخامت مناسب
        drawBuffer(tailVertexBuffer, tailVertexCount, GLES20.GL_LINES, 2.0f)

        // 3. رسم دایره‌های نقطه‌چین پالس‌زن
        drawBuffer(dotVertexBuffer, dotVertexCount, GLES20.GL_POINTS, 1.0f)

        // 4. رسم کامت‌ها
        drawBuffer(cometVertexBuffer, cometVertexCount, GLES20.GL_LINES, 2.5f)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisableVertexAttribArray(angleHandle)
        if (elementTypeHandle >= 0) GLES20.glDisableVertexAttribArray(elementTypeHandle)
        if (particleParamsHandle >= 0) GLES20.glDisableVertexAttribArray(particleParamsHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun setupGeometry() {
        fun createBuffer(coords: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(coords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(coords)
                position(0)
            }
        }

        // 1. Base Ring
        val ringSegments = 96
        val ringCoords = FloatArray(ringSegments * 2 * 11)
        var rIdx = 0
        for (i in 0 until ringSegments) {
            val a1 = ((2.0 * PI * i) / ringSegments).toFloat()
            val a2 = ((2.0 * PI * (i + 1)) / ringSegments).toFloat()
            val (r1, g1, b1) = getGradientColor(i.toFloat() / ringSegments)
            val (r2, g2, b2) = getGradientColor((i + 1).toFloat() / ringSegments)

            fun putV(angle: Float, r: Float, g: Float, b: Float) {
                ringCoords[rIdx++] = 0f; ringCoords[rIdx++] = 0f; ringCoords[rIdx++] = 0f
                ringCoords[rIdx++] = r; ringCoords[rIdx++] = g; ringCoords[rIdx++] = b; ringCoords[rIdx++] = 0.90f
                ringCoords[rIdx++] = angle
                ringCoords[rIdx++] = 0.0f
                ringCoords[rIdx++] = 0f; ringCoords[rIdx++] = 0f
            }
            putV(a1, r1, g1, b1)
            putV(a2, r2, g2, b2)
        }
        ringVertexBuffer = createBuffer(ringCoords)
        ringVertexCount = ringCoords.size / 11

        // 2. Pulsing Dots (GL_POINTS)
        val dotsCount = 64
        val dotCoords = FloatArray(dotsCount * 11)
        var dIdx = 0
        for (i in 0 until dotsCount) {
            val angle = ((2.0 * PI * i) / dotsCount).toFloat()
            val (red, green, blue) = getGradientColor(i.toFloat() / dotsCount)

            dotCoords[dIdx++] = 0f; dotCoords[dIdx++] = 0f; dotCoords[dIdx++] = 0f
            dotCoords[dIdx++] = red; dotCoords[dIdx++] = green; dotCoords[dIdx++] = blue; dotCoords[dIdx++] = 1.0f
            dotCoords[dIdx++] = angle
            dotCoords[dIdx++] = 1.0f // elementType = 1.0 (Dot)
            dotCoords[dIdx++] = 0f; dotCoords[dIdx++] = 0f
        }
        dotVertexBuffer = createBuffer(dotCoords)
        dotVertexCount = dotCoords.size / 11

        // 3. Trailing Tails (GL_LINES از خط پایه تا زیر دایره متحرک)
        val tailCount = 64
        val tailCoords = FloatArray(tailCount * 2 * 11)
        var tIdx = 0
        for (i in 0 until tailCount) {
            val angle = ((2.0 * PI * i) / tailCount).toFloat()
            val (red, green, blue) = getGradientColor(i.toFloat() / tailCount)

            fun putTailV(tParam: Float) {
                tailCoords[tIdx++] = tParam; tailCoords[tIdx++] = 0f; tailCoords[tIdx++] = 0f
                tailCoords[tIdx++] = red; tailCoords[tIdx++] = green; tailCoords[tIdx++] = blue; tailCoords[tIdx++] = 0.8f
                tailCoords[tIdx++] = angle
                tailCoords[tIdx++] = 1.5f // elementType = 1.5 (Tail)
                tailCoords[tIdx++] = 0f; tailCoords[tIdx++] = 0f
            }
            putTailV(0.0f) // نقطه پایینی روی خط پایه (t = 0.0)
            putTailV(1.0f) // نقطه بالایی متصل به زیر دایره (t = 1.0)
        }
        tailVertexBuffer = createBuffer(tailCoords)
        tailVertexCount = tailCoords.size / 11

        // 4. Comets
        val cometsCount = 48
        val cometCoords = FloatArray(cometsCount * 2 * 11)
        var cIdx = 0
        for (i in 0 until cometsCount) {
            val angleJitter = sin(i * 2.3).toFloat() * 0.15f
            val angle = ((2.0 * PI * i) / cometsCount).toFloat() + angleJitter
            val speed = 0.18f + (((1f + sin(i * 3.7).toFloat()) * 0.5f) * 0.22f)
            val timeOffset = ((1f + cos(i * 1.9).toFloat()) * 0.5f) * 3.0f
            val (red, green, blue) = getGradientColor(i.toFloat() / cometsCount)

            fun putCometV(tp: Float) {
                cometCoords[cIdx++] = tp; cometCoords[cIdx++] = 0f; cometCoords[cIdx++] = 0f
                cometCoords[cIdx++] = red; cometCoords[cIdx++] = green; cometCoords[cIdx++] = blue; cometCoords[cIdx++] = 0.7f
                cometCoords[cIdx++] = angle
                cometCoords[cIdx++] = 2.0f
                cometCoords[cIdx++] = speed; cometCoords[cIdx++] = timeOffset
            }
            putCometV(0.0f)
            putCometV(1.0f)
        }
        cometVertexBuffer = createBuffer(cometCoords)
        cometVertexCount = cometCoords.size / 11
    }

    private fun getGradientColor(t: Float): Triple<Float, Float, Float> {
        val normalizedT = ((t % 1.0f) + 1.0f) % 1.0f
        return when {
            normalizedT < 0.33f -> {
                val factor = normalizedT / 0.33f
                Triple(0.95f, 0.85f - (factor * 0.15f), 0.25f)
            }
            normalizedT < 0.66f -> {
                val factor = (normalizedT - 0.33f) / 0.33f
                Triple(0.80f - (factor * 0.20f), 0.70f + (factor * 0.15f), 0.25f + (factor * 0.25f))
            }
            else -> {
                val factor = (normalizedT - 0.66f) / 0.34f
                Triple(0.60f + (factor * 0.35f), 0.85f - (factor * 0.10f), 0.50f - (factor * 0.25f))
            }
        }
    }
}