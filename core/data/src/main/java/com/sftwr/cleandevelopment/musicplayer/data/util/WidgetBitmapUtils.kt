package com.sftwr.cleandevelopment.musicplayer.data.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.WindowManager
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * Production utility helper for widget bitmap downsampling, artwork caching with invalidation,
 * multi-source cover art extraction, memory protection, and RemoteViews memory limit compliance.
 */
object WidgetBitmapUtils {

    private const val TAG = "WidgetBitmapUtils"
    private const val BITMAP_CACHE_MAX_SIZE = 12
    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()

    /**
     * Estimates the maximum total RemoteViews bitmap memory cap allowed by system_server (6 * W * H).
     */
    fun calculateWidgetMemoryCapBytes(context: Context): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = context.getSystemService(WindowManager::class.java)?.currentWindowMetrics?.bounds
            if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                6L * bounds.width().toLong() * bounds.height().toLong()
            } else {
                fallbackCap(context)
            }
        } else {
            fallbackCap(context)
        }
    }

    private fun fallbackCap(context: Context): Long {
        val dm = context.resources.displayMetrics
        return 6L * dm.widthPixels.toLong() * dm.heightPixels.toLong()
    }

    /**
     * Decodes and downsamples a bitmap from a content URI, MediaStore album art URI, or embedded audio track metadata
     * with cache invalidation when the track URI changes.
     */
    fun decodeDownsampledWidgetBitmap(
        context: Context,
        uriString: String?,
        songId: Long? = null,
        maxWidthDp: Int = 180,
        maxHeightDp: Int = 180,
        config: Bitmap.Config = Bitmap.Config.RGB_565
    ): Bitmap {
        val cacheKey = "${songId ?: -1L}_${uriString.orEmpty()}_${maxWidthDp}_${maxHeightDp}"
        bitmapCache[cacheKey]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val density = context.resources.displayMetrics.density
        val targetWidthPx = max(1, (maxWidthDp * density).toInt())
        val targetHeightPx = max(1, (maxHeightDp * density).toInt())

        val decodedBitmap = tryDecodeBitmapFromMultiSource(context, uriString, songId, targetWidthPx, targetHeightPx, config)
            ?: createPlaceholderBitmap(maxWidthDp, maxHeightDp, context)

        if (bitmapCache.size > BITMAP_CACHE_MAX_SIZE) {
            bitmapCache.clear()
        }
        bitmapCache[cacheKey] = decodedBitmap
        return decodedBitmap
    }

    private fun tryDecodeBitmapFromMultiSource(
        context: Context,
        uriString: String?,
        songId: Long?,
        targetWidthPx: Int,
        targetHeightPx: Int,
        config: Bitmap.Config
    ): Bitmap? {
        val density = context.resources.displayMetrics.density
        val cornerRadiusPx = (14 * density).toInt()

        val parsedUri = if (!uriString.isNullOrBlank()) runCatching { Uri.parse(uriString) }.getOrNull() else null

        // Stage 1: Load Thumbnail via ContentResolver on API 29+ if URI exists
        if (parsedUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val size = Size(targetWidthPx, targetHeightPx)
                val thumb = context.contentResolver.loadThumbnail(parsedUri, size, null)
                return roundedCornerBitmap(thumb, cornerRadiusPx)
            }.onFailure {
                Log.d(TAG, "Stage 1 loadThumbnail failed for URI: $uriString")
            }
        }

        // Stage 2: Direct ContentResolver / File Stream decoding
        if (parsedUri != null) {
            decodeStreamScaled(context, parsedUri, targetWidthPx, targetHeightPx, config)?.let {
                return roundedCornerBitmap(it, cornerRadiusPx)
            }
        }

        // Stage 3: MediaStore Album Art via Numeric Album ID
        val albumId = uriString?.toLongOrNull()
        if (albumId != null && albumId > 0L) {
            val albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    val thumb = context.contentResolver.loadThumbnail(albumArtUri, Size(targetWidthPx, targetHeightPx), null)
                    return roundedCornerBitmap(thumb, cornerRadiusPx)
                }
            }
            decodeStreamScaled(context, albumArtUri, targetWidthPx, targetHeightPx, config)?.let {
                return roundedCornerBitmap(it, cornerRadiusPx)
            }
        }

        // Stage 4: Load MediaStore Audio Track Thumbnail by songId
        if (songId != null && songId > 0L) {
            val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    val thumb = context.contentResolver.loadThumbnail(trackUri, Size(targetWidthPx, targetHeightPx), null)
                    return roundedCornerBitmap(thumb, cornerRadiusPx)
                }.onFailure {
                    Log.d(TAG, "Stage 4 loadThumbnail failed for songId: $songId")
                }
            }

            // Embedded picture extraction via MediaMetadataRetriever
            runCatching {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, trackUri)
                val rawBytes = mmr.embeddedPicture
                mmr.release()

                if (rawBytes != null && rawBytes.isNotEmpty()) {
                    val opts = BitmapFactory.Options().apply { inPreferredConfig = config }
                    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)?.let {
                        return roundedCornerBitmap(it, cornerRadiusPx)
                    }
                }
            }.onFailure {
                Log.d(TAG, "Stage 4 MediaMetadataRetriever failed for songId: $songId")
            }
        }

        // Stage 5: Embedded picture from parsedUri
        if (parsedUri != null) {
            runCatching {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, parsedUri)
                val rawBytes = mmr.embeddedPicture
                mmr.release()

                if (rawBytes != null && rawBytes.isNotEmpty()) {
                    val opts = BitmapFactory.Options().apply { inPreferredConfig = config }
                    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)?.let {
                        return roundedCornerBitmap(it, cornerRadiusPx)
                    }
                }
            }
        }

        return null
    }

    private fun decodeStreamScaled(
        context: Context,
        uri: Uri,
        targetWidthPx: Int,
        targetHeightPx: Int,
        config: Bitmap.Config
    ): Bitmap? {
        return runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInputStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            var sampleSize = 1
            while ((options.outWidth / (sampleSize * 2) >= targetWidthPx && options.outHeight / (sampleSize * 2) >= targetHeightPx) ||
                (options.outWidth / sampleSize > targetWidthPx * 2) ||
                (options.outHeight / sampleSize > targetHeightPx * 2)
            ) {
                sampleSize *= 2
            }

            val bytesPerPixel = 2L
            val perImageCapBytes = calculateWidgetMemoryCapBytes(context) / 4L
            while ((max(1, options.outWidth / sampleSize).toLong() * max(1, options.outHeight / sampleSize).toLong() * bytesPerPixel) > perImageCapBytes) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = config
            }

            openInputStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }.getOrNull()
    }

    private fun openInputStream(context: Context, uri: Uri): InputStream? {
        return runCatching {
            if (uri.scheme == ContentResolver.SCHEME_CONTENT || uri.scheme == ContentResolver.SCHEME_FILE) {
                context.contentResolver.openInputStream(uri)
            } else {
                java.io.File(uri.path ?: "").takeIf { it.exists() }?.inputStream()
            }
        }.getOrNull()
    }

    /**
     * Rounds corners for modern glass aesthetic.
     */
    fun roundedCornerBitmap(src: Bitmap, cornerRadiusPx: Int): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }
        val rect = Rect(0, 0, src.width, src.height)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, cornerRadiusPx.toFloat(), cornerRadiusPx.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        return output
    }

    /**
     * Generates a clean, crisp monochrome vinyl record & tonearm turntable graphic matching reference aesthetic.
     */
    fun createPlaceholderBitmap(maxWidthDp: Int, maxHeightDp: Int, context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        val widthPx = max(120, (maxWidthDp * density).toInt())
        val heightPx = max(120, (maxHeightDp * density).toInt())

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cornerRadius = 14 * density

        // Base Glassmorphic Dark Card
        val bgShader = LinearGradient(
            0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
            Color.parseColor("#161C26"),
            Color.parseColor("#0B0E14"),
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = bgShader
        }
        val rectF = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)

        // Subtle Glass Border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
            color = Color.parseColor("#263445")
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

        // Turntable Geometry Calculations
        val centerX = widthPx * 0.44f
        val centerY = heightPx * 0.52f
        val recordRadius = min(widthPx, heightPx) * 0.40f

        // Outer Vinyl Disc
        val discPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#10141D")
        }
        canvas.drawCircle(centerX, centerY, recordRadius, discPaint)

        // Crisp Concentric Vinyl Grooves
        val groovePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.2f * density
            color = Color.parseColor("#2A3648")
        }
        val grooveLightPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.0f * density
            color = Color.parseColor("#3B4A60")
        }

        canvas.drawCircle(centerX, centerY, recordRadius * 0.90f, groovePaint)
        canvas.drawCircle(centerX, centerY, recordRadius * 0.80f, grooveLightPaint)
        canvas.drawCircle(centerX, centerY, recordRadius * 0.70f, groovePaint)
        canvas.drawCircle(centerX, centerY, recordRadius * 0.60f, grooveLightPaint)
        canvas.drawCircle(centerX, centerY, recordRadius * 0.50f, groovePaint)

        // Center White Vinyl Record Label Circle
        val centerLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F1F5F9")
        }
        canvas.drawCircle(centerX, centerY, recordRadius * 0.30f, centerLabelPaint)

        // Center Spindle Hole
        val holePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#10141D")
        }
        canvas.drawCircle(centerX, centerY, recordRadius * 0.08f, holePaint)

        // Tonearm Pivot & Rod Assembly
        val pivotX = widthPx * 0.82f
        val pivotY = heightPx * 0.22f

        // Pivot Base Circle
        val pivotPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
        }
        canvas.drawCircle(pivotX, pivotY, 5 * density, pivotPaint)

        val pivotInnerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#00E5FF")
        }
        canvas.drawCircle(pivotX, pivotY, 2.5f * density, pivotInnerPaint)

        // Metallic Tonearm Shaft Line
        val armPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
            color = Color.parseColor("#CBD5E1")
            strokeCap = Paint.Cap.ROUND
        }

        val armPath = Path().apply {
            moveTo(pivotX, pivotY)
            lineTo(pivotX - 6 * density, pivotY + 22 * density)
            lineTo(centerX + recordRadius * 0.20f, centerY - recordRadius * 0.20f)
        }
        canvas.drawPath(armPath, armPaint)

        // Tonearm Cartridge / Headshell Box
        val cartridgePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E2E8F0")
        }
        val cartridgeX = centerX + recordRadius * 0.20f
        val cartridgeY = centerY - recordRadius * 0.20f

        canvas.save()
        canvas.rotate(-35f, cartridgeX, cartridgeY)
        val cartridgeRect = RectF(
            cartridgeX - 3 * density,
            cartridgeY - 6 * density,
            cartridgeX + 3 * density,
            cartridgeY + 6 * density
        )
        canvas.drawRoundRect(cartridgeRect, 2 * density, 2 * density, cartridgePaint)
        canvas.restore()

        return bitmap
    }
}
