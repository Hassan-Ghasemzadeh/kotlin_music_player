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
import kotlin.math.sin

/**
 * OpenGL ES 2.0 Renderer for 'DynamicScale' visualizer.
 * Renders a circular ring consisting of 64 discrete radial segments (frequency bars),
 * an inner perimeter base ring, and outer peak glow ticks.
 * The outer lengths of the bars scale dynamically in real time based on audio FFT frequency bands.
 */
class DynamicScaleRenderer : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var programId = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var angleHandle = 0
    private var isOuterHandle = 0
    private var elementTypeHandle = 0

    private var mvpMatrixHandle = 0
    private var timeHandle = 0
    private var fftValuesHandle = 0
    private var bassHandle = 0
    private var averageFftHandle = 0

    private var vertexBuffer: FloatBuffer? = null
    private var totalVertexCount = 0

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
            attribute float vIsOuter;
            attribute float vElementType; // 0.0 = bar, 1.0 = inner ring, 2.0 = outer peak tick
            
            uniform mat4 uMVPMatrix;
            uniform float u_time;
            uniform float u_bassValue;
            uniform float u_averageFft;
            uniform float u_fftValues[32];
            
            varying vec4 fragColor;
            
            void main() {
                vec4 pos = vec4(0.0, 0.0, 0.0, 1.0);
                
                // Normalize angle [0, 2PI] to [0, 1) range for FFT band lookup
                float normAngle = vAngle / 6.28318530718;
                normAngle = fract(normAngle);
                if (normAngle < 0.0) normAngle += 1.0;
                
                int fftIdx = int(floor(normAngle * 32.0));
                if (fftIdx < 0) fftIdx = 0;
                if (fftIdx > 31) fftIdx = 31;
                
                float bandFft = u_fftValues[fftIdx];
                
                float baseRadius = 1.04;
                float maxBarHeight = 0.24;
                
                // Frequency-reactive bar extension
                float barHeight = 0.012 + (bandFft * maxBarHeight) + (u_bassValue * 0.05);
                
                float r = baseRadius;
                if (vElementType < 0.5) {
                    // 1. Radial Frequency Bar
                    r = baseRadius + (vIsOuter * barHeight);
                } else if (vElementType < 1.5) {
                    // 2. Inner Perimeter Base Ring
                    r = baseRadius;
                } else {
                    // 3. Outer Peak Glow Tick
                    r = baseRadius + barHeight + 0.015;
                }
                
                pos = vec4(r * cos(vAngle), r * sin(vAngle), 0.0, 1.0);
                
                gl_Position = uMVPMatrix * pos;
                
                vec4 col = vColor;
                // Dynamic audio-reactive color brightness boost
                col.rgb *= (0.85 + (bandFft * 0.35) + (u_bassValue * 0.35));
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

        setupBarsGeometry()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(projectionMatrix, 0)

        val minDim = minOf(width, height).toFloat()
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
        isOuterHandle = GLES20.glGetAttribLocation(programId, "vIsOuter")
        elementTypeHandle = GLES20.glGetAttribLocation(programId, "vElementType")

        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        timeHandle = GLES20.glGetUniformLocation(programId, "u_time")
        fftValuesHandle = GLES20.glGetUniformLocation(programId, "u_fftValues")
        bassHandle = GLES20.glGetUniformLocation(programId, "u_bassValue")
        averageFftHandle = GLES20.glGetUniformLocation(programId, "u_averageFft")

        GLES20.glUniform1f(timeHandle, timeSec)
        if (bassHandle >= 0) GLES20.glUniform1f(bassHandle, bassValue)
        if (averageFftHandle >= 0) GLES20.glUniform1f(averageFftHandle, averageFft)
        if (fftValuesHandle >= 0) GLES20.glUniform1fv(fftValuesHandle, 32, smoothedFftData, 0)

        GLES20.glLineWidth(4.0f)
        val stride = 10 * 4

        // Slow rotation for orbital dynamics
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, timeSec * 6f, 0f, 0f, 1f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        vertexBuffer?.let { buffer ->
            buffer.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, buffer)

            buffer.position(3)
            GLES20.glEnableVertexAttribArray(colorHandle)
            GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, stride, buffer)

            buffer.position(7)
            GLES20.glEnableVertexAttribArray(angleHandle)
            GLES20.glVertexAttribPointer(angleHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)

            if (isOuterHandle >= 0) {
                buffer.position(8)
                GLES20.glEnableVertexAttribArray(isOuterHandle)
                GLES20.glVertexAttribPointer(isOuterHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)
            }

            if (elementTypeHandle >= 0) {
                buffer.position(9)
                GLES20.glEnableVertexAttribArray(elementTypeHandle)
                GLES20.glVertexAttribPointer(elementTypeHandle, 1, GLES20.GL_FLOAT, false, stride, buffer)
            }

            GLES20.glDrawArrays(GLES20.GL_LINES, 0, totalVertexCount)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisableVertexAttribArray(angleHandle)
        if (isOuterHandle >= 0) GLES20.glDisableVertexAttribArray(isOuterHandle)
        if (elementTypeHandle >= 0) GLES20.glDisableVertexAttribArray(elementTypeHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun setupBarsGeometry() {
        val barsCount = 64
        val ringSegments = 96

        // 10 floats per vertex: x, y, z, r, g, b, a, angle, isOuter, elementType
        val maxVertices = (barsCount * 2) + (ringSegments * 2) + (barsCount * 2)
        val coords = FloatArray(maxVertices * 10)
        var index = 0

        fun addVertex(
            angle: Float,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float,
            isOuter: Float,
            elementType: Float,
        ) {
            coords[index++] = 0f; coords[index++] = 0f; coords[index++] = 0f
            coords[index++] = red; coords[index++] = green; coords[index++] = blue; coords[index++] = alpha
            coords[index++] = angle
            coords[index++] = isOuter
            coords[index++] = elementType
        }

        // 1. Radial Frequency Bars (64 bars)
        for (i in 0 until barsCount) {
            val angle = ((2.0 * PI * i.toDouble()) / barsCount.toDouble()).toFloat()
            val t = i.toFloat() / barsCount
            val (r, g, b) = getGradientColor(t)

            // Inner base vertex
            addVertex(angle, r, g, b, 0.90f, 0.0f, 0.0f)
            // Outer tip vertex
            addVertex(angle, r, g, b, 0.95f, 1.0f, 0.0f)
        }

        // 2. Inner Perimeter Base Ring (96 segments)
        for (i in 0 until ringSegments) {
            val angle1 = ((2.0 * PI * i.toDouble()) / ringSegments.toDouble()).toFloat()
            val angle2 = ((2.0 * PI * (i + 1).toDouble()) / ringSegments.toDouble()).toFloat()

            val t1 = i.toFloat() / ringSegments
            val (r1, g1, b1) = getGradientColor(t1)
            val t2 = (i + 1).toFloat() / ringSegments
            val (r2, g2, b2) = getGradientColor(t2)

            addVertex(angle1, r1, g1, b1, 0.70f, 0.0f, 1.0f)
            addVertex(angle2, r2, g2, b2, 0.70f, 0.0f, 1.0f)
        }

        // 3. Outer Peak Ticks (64 ticks hovering above bar tips)
        for (i in 0 until barsCount) {
            val angle = ((2.0 * PI * i.toDouble()) / barsCount.toDouble()).toFloat()
            val t = i.toFloat() / barsCount
            val (r, g, b) = getGradientColor(t)

            addVertex(angle, r, g, b, 0.85f, 1.0f, 2.0f)
            addVertex(angle + 0.015f, r, g, b, 0.85f, 1.0f, 2.0f)
        }

        totalVertexCount = index / 10
        vertexBuffer = ByteBuffer.allocateDirect(index * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(coords, 0, index)
            position(0)
        }
    }

    private fun getGradientColor(t: Float): Triple<Float, Float, Float> {
        val normalizedT = ((t % 1.0f) + 1.0f) % 1.0f
        return when {
            normalizedT < 0.33f -> {
                val factor = normalizedT / 0.33f
                Triple(1.0f, 0.75f - (factor * 0.40f), 0.15f) // Gold to Warm Orange
            }
            normalizedT < 0.66f -> {
                val factor = (normalizedT - 0.33f) / 0.33f
                Triple(1.0f - (factor * 0.35f), 0.35f - (factor * 0.15f), 0.15f + (factor * 0.65f)) // Orange to Magenta
            }
            else -> {
                val factor = (normalizedT - 0.66f) / 0.34f
                Triple(0.65f + (factor * 0.35f), 0.20f + (factor * 0.55f), 0.80f - (factor * 0.65f)) // Magenta to Gold
            }
        }
    }
}
