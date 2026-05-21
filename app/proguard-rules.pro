# ProGuard rules for Lock-Chat

# Keep Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep ZXing
-keep class com.google.zxing.** { *; }

# Keep Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep domain models (no obfuscar para Room y Hilt)
-keep class com.lockchat.app.domain.** { *; }
-keep class com.lockchat.app.data.local.entity.** { *; }
