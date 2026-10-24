# Keep JAudioTagger from being obfuscated or stripped
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# Keep your data/domain models safe if needed (e.g., Song model)
-keep class com.sftwr.cleandevelopment.musicplayer.domain.model.** { *; }