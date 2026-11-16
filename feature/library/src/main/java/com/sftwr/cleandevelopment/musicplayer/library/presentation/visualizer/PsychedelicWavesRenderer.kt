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
 * High-performance OpenGL ES 2.0 Renderer for 'Psychedelic Waves' visualizer.
 * Renders three distinct audio-reactive rings with asynchronous, independent pulse dynamics and muted pastel palettes.
 */
class PsychedelicWavesRenderer : GLSurfaceView.Renderer {

    private var programId = 0
    private var resolutionHandle = 0
    private var timeHandle = 0
    private var audioAmplitudeHandle = 0
    private var rotationAngleHandle = 0
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
            uniform float u_RotationAngle;
            uniform float u_FftBands[16];

            varying vec2 v_UV;

            float sampleFft(float normAngle) {
                float pos = fract(normAngle) * 16.0;
                int idx0 = int(floor(pos));
                float t = fract(pos);
                t = t * t * (3.0 - 2.0 * t);

                float v0 = u_FftBands[0];
                float v1 = u_FftBands[1];

                if (idx0 == 0) { v0 = u_FftBands[0]; v1 = u_FftBands[1]; }
                else if (idx0 == 1) { v0 = u_FftBands[1]; v1 = u_FftBands[2]; }
                else if (idx0 == 2) { v0 = u_FftBands[2]; v1 = u_FftBands[3]; }
                else if (idx0 == 3) { v0 = u_FftBands[3]; v1 = u_FftBands[4]; }
                else if (idx0 == 4) { v0 = u_FftBands[4]; v1 = u_FftBands[5]; }
                else if (idx0 == 5) { v0 = u_FftBands[5]; v1 = u_FftBands[6]; }
                else if (idx0 == 6) { v0 = u_FftBands[6]; v1 = u_FftBands[7]; }
                else if (idx0 == 7) { v0 = u_FftBands[7]; v1 = u_FftBands[8]; }
                else if (idx0 == 8) { v0 = u_FftBands[8]; v1 = u_FftBands[9]; }
                else if (idx0 == 9) { v0 = u_FftBands[9]; v1 = u_FftBands[10]; }
                else if (idx0 == 10) { v0 = u_FftBands[10]; v1 = u_FftBands[11]; }
                else if (idx0 == 11) { v0 = u_FftBands[11]; v1 = u_FftBands[12]; }
                else if (idx0 == 12) { v0 = u_FftBands[12]; v1 = u_FftBands[13]; }
                else if (idx0 == 13) { v0 = u_FftBands[13]; v1 = u_FftBands[14]; }
                else if (idx0 == 14) { v0 = u_FftBands[14]; v1 = u_FftBands[15]; }
                else { v0 = u_FftBands[15]; v1 = u_FftBands[0]; }

                return mix(v0, v1, t);
            }

            void main() {
                vec2 st = v_UV - 0.5;

                if (u_Resolution.x > u_Resolution.y) {
                    st.x *= u_Resolution.x / u_Resolution.y;
                } else {
                    st.y *= u_Resolution.y / u_Resolution.x;
                }
                
                float r = length(st) * 2.0;

                // پاکسازی مرکز قاب عکس و حریم بیرونی
                if (r < 0.72 || r > 1.45) {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
                    return;
                }

                float rawAngle = atan(st.y, st.x);
                float normAngle = fract((rawAngle + 3.14159265359) / 6.28318530718);

                // پالت‌های رنگی متمایز و ملایم (Pastel & Muted)
                vec3 layer1ColorA = vec3(0.85, 0.60, 0.30); // کهربایی گرم
                vec3 layer1ColorB = vec3(0.90, 0.40, 0.35); // مرجانی ملایم

                vec3 layer2ColorA = vec3(0.40, 0.72, 0.45); // سبز زیتونی / نعنایی
                vec3 layer2ColorB = vec3(0.30, 0.68, 0.60); // سبز یشمی روشن

                vec3 layer3ColorA = vec3(0.30, 0.55, 0.80); // آبی ملایم
                vec3 layer3ColorB = vec3(0.60, 0.45, 0.75); // یاسی مات

                // --- حلقه اول (داخلی - پالس مستقل مبتنی بر زاویه خود و زمان) ---
                float angle1 = rawAngle + u_Time * 0.25;
                float localFft1 = sampleFft(fract(normAngle + u_Time * 0.08));
                float localAmp1 = clamp(localFft1 * 1.2, 0.0, 1.0);

                float baseR1 = 0.84;
                float peakR1 = baseR1 + (localAmp1 * 0.08);
                
                float fillFactor1 = step(baseR1, r) * step(r, peakR1);
                float edgeDist1 = min(abs(r - baseR1), abs(r - peakR1));
                float glow1 = exp(-edgeDist1 * 30.0) * 0.25;

                vec3 color1 = mix(layer1ColorA, layer1ColorB, 0.5 + 0.5 * sin(angle1 * 2.0 + u_Time));
                vec4 ring1 = vec4(color1 * (0.95 + glow1), clamp(fillFactor1 * 0.70 + glow1 * 0.25, 0.0, 1.0));


                // --- حلقه دوم (میانی - پالس غیرهمزمان با شیفت فاز زاویه‌ای و زمانی) ---
                float angle2 = rawAngle - u_Time * 0.35;
                float localFft2 = sampleFft(fract(normAngle + 0.35 - u_Time * 0.12));
                float localAmp2 = clamp(localFft2 * 1.1, 0.0, 1.0);

                float baseR2 = 0.85;
                float peakR2 = baseR2 + (localAmp2 * 0.07);

                float fillFactor2 = step(baseR2, r) * step(r, peakR2);
                float edgeDist2 = min(abs(r - baseR2), abs(r - peakR2));
                float glow2 = exp(-edgeDist2 * 30.0) * 0.25;

                vec3 color2 = mix(layer2ColorA, layer2ColorB, 0.5 + 0.5 * cos(angle2 * 2.5 - u_Time * 1.3));
                vec4 ring2 = vec4(color2 * (0.95 + glow2), clamp(fillFactor2 * 0.68 + glow2 * 0.25, 0.0, 1.0));


                // --- حلقه سوم (بیرونی - پالس کاملاً مستقل و مختص به خود) ---
                float angle3 = rawAngle + u_Time * 0.45;
                float localFft3 = sampleFft(fract(normAngle + 0.70 + u_Time * 0.15));
                float localAmp3 = clamp(localFft3 * 1.0, 0.0, 1.0);

                float baseR3 = 0.86;
                float peakR3 = baseR3 + (localAmp3 * 0.06);

                float fillFactor3 = step(baseR3, r) * step(r, peakR3);
                float edgeDist3 = min(abs(r - baseR3), abs(r - peakR3));
                float glow3 = exp(-edgeDist3 * 30.0) * 0.25;

                vec3 color3 = mix(layer3ColorA, layer3ColorB, 0.5 + 0.5 * sin(angle3 * 3.0 + u_Time * 1.5));
                vec4 ring3 = vec4(color3 * (0.90 + glow3), clamp(fillFactor3 * 0.65 + glow3 * 0.22, 0.0, 1.0));


                // ترکیب سه لایه بدون تداخل و با شفافیت ملایم
                vec4 accumColor = ring1;
                accumColor.rgb = mix(accumColor.rgb, ring2.rgb, ring2.a);
                accumColor.a = max(accumColor.a, ring2.a);

                accumColor.rgb = mix(accumColor.rgb, ring3.rgb, ring3.a);
                accumColor.a = max(accumColor.a, ring3.a);

                float viewportFade = 1.0 - smoothstep(1.20, 1.45, r);
                accumColor.a *= viewportFade;

                gl_FragColor = accumColor;
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

        val currentTime = SystemClock.elapsedRealtime()
        val timeSec = (currentTime - startTime) / 1000f
        val rotationAngle = (timeSec * 0.3f) % (2.0f * Math.PI.toFloat())

        synchronized(fftLock) {
            for (i in smoothedFftData.indices) {
                val target = rawFftData[i]
                smoothedFftData[i] = (smoothedFftData[i] * 0.70f) + (target * 0.30f)
            }
        }

        val audioAmplitude = smoothedFftData.average().toFloat()

        GLES20.glUseProgram(programId)

        resolutionHandle = GLES20.glGetUniformLocation(programId, "u_Resolution")
        timeHandle = GLES20.glGetUniformLocation(programId, "u_Time")
        audioAmplitudeHandle = GLES20.glGetUniformLocation(programId, "u_AudioAmplitude")
        rotationAngleHandle = GLES20.glGetUniformLocation(programId, "u_RotationAngle")
        fftBandsHandle = GLES20.glGetUniformLocation(programId, "u_FftBands")

        if (resolutionHandle >= 0) GLES20.glUniform2f(resolutionHandle, viewportWidth.toFloat(), viewportHeight.toFloat())
        if (timeHandle >= 0) GLES20.glUniform1f(timeHandle, timeSec)
        if (audioAmplitudeHandle >= 0) GLES20.glUniform1f(audioAmplitudeHandle, audioAmplitude)
        if (rotationAngleHandle >= 0) GLES20.glUniform1f(rotationAngleHandle, rotationAngle)
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

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        if (vertexShader == 0 || fragmentShader == 0) return 0

        val program = GLES20.glCreateProgram()
        if (program == 0) return 0

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)

        GLES20.glBindAttribLocation(program, 0, "a_Position")
        GLES20.glBindAttribLocation(program, 1, "a_TexCoord")

        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun setupQuadGeometry() {
        val positions = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f,  1.0f,
            1.0f,  1.0f,
        )

        val texCoords = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
        )

        positionBuffer = ByteBuffer.allocateDirect(positions.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(positions)
                position(0)
            }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords)
                position(0)
            }
    }
}