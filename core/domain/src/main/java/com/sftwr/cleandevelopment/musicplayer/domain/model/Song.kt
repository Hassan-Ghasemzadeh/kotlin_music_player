package com.sftwr.cleandevelopment.musicplayer.domain.model

import android.net.Uri

data class Song(
    val id: Long,                   // شناسه یکتای فایل در MediaStore (ضروری برای لود آهنگ)
    val title: String,              // عنوان آهنگ
    val artist: String,             // نام خواننده/هنرمند
    val album: String,              // نام آلبوم
    val albumId: Long,              // شناسه آلبوم (برای استخراج کاور واقعی از ContentResolver)
    val contentUri: Uri,            // آدرس Uri مستقیم برای دادن به ExoPlayer جهت پخش
    val albumArtUri: String? = null,// آدرس URI یا URL کاور آلبوم (امکان null بودن در صورت نداشتن عکس)
    val duration: Long = 0L,        // مدت زمان آهنگ به میلی‌ثانیه
    val size: Long = 0L,            // حجم فایل به بایت
    val path: String = ""           // مسیر کامل فایل در حافظه (برای مدیریت فیلترها یا پوشه‌ها)
)
