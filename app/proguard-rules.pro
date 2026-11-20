# ====================================================================
# Android & Jetpack General Optimization Rules
# ====================================================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# ====================================================================
# Jetpack Compose Keep Rules
# ====================================================================
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-dontwarn androidx.compose.**

# ====================================================================
# Tapsell Mediation & Ad SDK Keep Rules
# ====================================================================
-keep class ir.tapsell.sdk.** { *; }
-keep class ir.tapsell.plus.** { *; }
-keep class ir.tapsell.mediation.** { *; }
-keep interface ir.tapsell.sdk.** { *; }
-keepclassmembers class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**

# ====================================================================
# AndroidX Media3 & ExoPlayer Keep Rules
# ====================================================================
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.decoder.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class * extends androidx.media3.session.MediaSessionService
-keep class * extends androidx.media3.session.MediaLibraryService
-dontwarn androidx.media3.**

# ====================================================================
# Android Native Audio Effects (Equalizer, BassBoost, Virtualizer, Reverb)
# ====================================================================
-keep class android.media.audiofx.** { *; }
-keepclassmembers class android.media.audiofx.** { *; }

# ====================================================================
# Room Persistence Library & Local Data Entities
# ====================================================================
-keep class com.sftwr.cleandevelopment.musicplayer.data.model.** { *; }
-keep class com.sftwr.cleandevelopment.musicplayer.data.source.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.ColumnInfo *;
}
-dontwarn androidx.room.**

# ====================================================================
# DataStore & Preference Storage Rules
# ====================================================================
-keep class androidx.datastore.preferences.** { *; }
-dontwarn androidx.datastore.**

# ====================================================================
# Dependency Injection (Hilt / Dagger) & ViewModels
# ====================================================================
-keep class com.sftwr.cleandevelopment.musicplayer.di.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @jakarta.inject.Inject <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel

# ====================================================================
# Jetpack WorkManager Keep Rules
# ====================================================================
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class * extends androidx.work.Worker {
    public <init>(...);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}
-dontwarn androidx.work.**

# ====================================================================
# Coil Image Loading Rules
# ====================================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ====================================================================
# Domain & Shared Models
# ====================================================================
-keep class com.sftwr.cleandevelopment.musicplayer.domain.model.** { *; }
-keep class com.sftwr.cleandevelopment.musicplayer.model.** { *; }

# ====================================================================
# KotlinX Serialization Rules
# ====================================================================
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** serializer(...);
}

# ====================================================================
# JAudioTagger Library Rules
# ====================================================================
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# ====================================================================
# Jetpack Glance Widgets, Receivers, and ActionCallbacks
# ====================================================================
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**
