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
-dontwarn androidx.room.**

# ====================================================================
# Dependency Injection (Hilt / Dagger) & ViewModels
# ====================================================================
-keep class com.sftwr.cleandevelopment.musicplayer.di.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel

# ====================================================================
# Domain & Shared Models
# ====================================================================
-keep class com.sftwr.cleandevelopment.musicplayer.domain.model.** { *; }
-keep class com.sftwr.cleandevelopment.musicplayer.model.** { *; }

# ====================================================================
# KotlinX Serialization & Annotation Attributes
# ====================================================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# ====================================================================
# JAudioTagger Library Rules
# ====================================================================
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# ====================================================================
# Jetpack Glance Widgets & ActionCallbacks
# ====================================================================
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
-keep class androidx.glance.appwidget.** { *; }

