package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * High-performance OpenGL ES 2.0 Renderer for 'Fantasy Planet' visualizer.
 * Renders ultra-thin expanding orbital rings starting tangentially right on the outer album border.
 */
class FantasyPlanetRenderer : GLSurfaceView.Renderer {

    private var programId = 0
    private var resolutionHandle = 0
    private var timeHandle = 0
    private var audioAmplitudeHandle = 0
    private var fftBandsHandle = 0

    private var positionBuffer: FloatBuffer? = null
    private var texCoordBuffer: FloatBuffer? = null

    private var rawFftData: FloatArray = FloatArray(16) { 0.25f }
    private var smoothedFftData: FloatArray = FloatArray(16) { 0.25f }
    private val fftLock = Any()

    private var startTime = SystemClock.elapsedRealtime()
    private var viewportWidth = 1080
    private var viewportHeight = 1080

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
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_UV;
            void main() {
                v_UV = a_TexCoord;
                gl_Position = a_Position;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif

            uniform vec2 u_Resolution;
            uniform float u_Time;
            uniform float u_AudioAmplitude;
            uniform float u_FftBands[16];
            varying vec2 v_UV;

            void main() {
                vec2 st = v_UV - 0.5;
                if (u_Resolution.x > u_Resolution.y) {
                    st.x *= u_Resolution.x / u_Resolution.y;
                } else {
                    st.y *= u_Resolution.y / u_Resolution.x;
                }

                float r = length(st) * 2.0;

                // پاکسازی مرکز دقیقاً مماس با لبه بیرونی بوردر عکس (0.80)
                if (r < 0.80) {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
                    return;
                }

                vec4 finalColor = vec4(0.0);

                const int RING_COUNT = 5;
                float cycleDuration = 6.5; 

                for (int i = 0; i < RING_COUNT; i++) {
                    float floatI = float(i);
                    float timeOffset = floatI * (cycleDuration / float(RING_COUNT));
                    float t = mod(u_Time + timeOffset, cycleDuration);
                    float age = t / cycleDuration; // از 0.0 تا 1.0

                    // شروع دقیق مماس بر لبه بوردر آلبوم (0.80) و انبساط به سمت بیرون
                    float currentR = 0.84 + age * 0.32;

                    // ضخامت بسیار نازک و ظریف خط
                    float ringDist = abs(r - currentR);
                    float thickness = 0.003; 

                    // تنظیم بازه محو شدن نرم و هماهنگ
                    float lifeAlpha = smoothstep(0.0, 0.04, age) * (1.0 - smoothstep(0.45, 0.80, age));

                    // رسم خط بسیار نازک
                    float lineAlpha = (1.0 - smoothstep(0.0, thickness + 0.002, ringDist)) * lifeAlpha * 0.55;
                    vec3 ringColor = mix(vec3(0.5, 0.75, 1.0), vec3(0.85, 0.6, 1.0), fract(floatI * 0.25 + u_Time * 0.03));
                    finalColor += vec4(ringColor * lineAlpha, lineAlpha);

                    // --- دایره کوچک متحرک دقیقاً سوار بر روی خط ---
                    float particleAngle = u_Time * (0.12 + floatI * 0.02) + floatI * 2.3;
                    vec2 particlePos = vec2(cos(particleAngle), sin(particleAngle)) * currentR;
                    float distToParticle = length(st * 2.0 - particlePos);

                    float dot = (1.0 - smoothstep(0.010, 0.020, distToParticle)) * lifeAlpha;
                    finalColor += vec4(vec3(1.0, 0.95, 0.85) * dot, dot * 0.80);
                }

                // محو ملایم لبه‌های بیرونی صفحه
                float outerFade = 1.0 - smoothstep(1.18, 1.42, r);
                finalColor.a *= outerFade;

                gl_FragColor = finalColor;
            }
        """.trimIndent()

        programId = createProgram(vertexShaderCode, fragmentShaderCode)
        setupQuadGeometry()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        if (width > 0 && height > 0) {
            viewportWidth = width
            viewportHeight = height
            GLES20.glViewport(0, 0, width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (programId == 0) return

        val timeSec = (SystemClock.elapsedRealtime() - startTime) / 1000f

        synchronized(fftLock) {
            for (i in smoothedFftData.indices) {
                smoothedFftData[i] = smoothedFftData[i] * 0.7f + rawFftData[i] * 0.3f
            }
        }

        val audioAmplitude = smoothedFftData.average().toFloat()

        GLES20.glUseProgram(programId)

        resolutionHandle = GLES20.glGetUniformLocation(programId, "u_Resolution")
        timeHandle = GLES20.glGetUniformLocation(programId, "u_Time")
        audioAmplitudeHandle = GLES20.glGetUniformLocation(programId, "u_AudioAmplitude")
        fftBandsHandle = GLES20.glGetUniformLocation(programId, "u_FftBands")

        if (resolutionHandle >= 0) GLES20.glUniform2f(resolutionHandle, viewportWidth.toFloat(), viewportHeight.toFloat())
        if (timeHandle >= 0) GLES20.glUniform1f(timeHandle, timeSec)
        if (audioAmplitudeHandle >= 0) GLES20.glUniform1f(audioAmplitudeHandle, audioAmplitude)
        if (fftBandsHandle >= 0) GLES20.glUniform1fv(fftBandsHandle, 16, smoothedFftData, 0)

        positionBuffer?.let { posBuf ->
            posBuf.position(0)
            GLES20.glEnableVertexAttribArray(0)
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, posBuf)
        }

        texCoordBuffer?.let { texBuf ->
            texBuf.position(0)
            GLES20.glEnableVertexAttribArray(1)
            GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, texBuf)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(0)
        GLES20.glDisableVertexAttribArray(1)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        if (vShader == 0 || fShader == 0) return 0
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vShader)
        GLES20.glAttachShader(prog, fShader)
        GLES20.glBindAttribLocation(prog, 0, "a_Position")
        GLES20.glBindAttribLocation(prog, 1, "a_TexCoord")
        GLES20.glLinkProgram(prog)
        return prog
    }

    private fun setupQuadGeometry() {
        val positions = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        val texCoords = floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f)
        positionBuffer = ByteBuffer.allocateDirect(positions.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(positions); position(0)
        }
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(texCoords); position(0)
        }
    }
}