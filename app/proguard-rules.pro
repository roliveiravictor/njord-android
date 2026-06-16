# Preserve the app's own package (data models parsed via JSONObject reflection-free)
-keep class com.njord.mobile.** { *; }

# Keep Kotlin metadata so reflection-based tools and coroutines work correctly
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose — keep entry points and lambda targets that the runtime calls reflectively
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# org.json is part of the Android SDK; no keep rules needed (never shrunk)
