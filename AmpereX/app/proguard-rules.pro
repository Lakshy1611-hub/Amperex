# AmpereX Proguard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn kotlinx.serialization.**
