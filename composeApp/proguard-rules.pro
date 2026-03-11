# ProGuard rules for CFSeeker
# Generated to protect serialization and maintain functionality

#---------------------------------
# Preserve line numbers for debugging stack traces (Firebase Crashlytics)
#---------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#---------------------------------
# kotlinx.serialization Rules
#---------------------------------

-keepattributes Signature
-keepattributes *Annotation*

# Keep serializers
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class com.dush1729.cfseeker.data.remote.model.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

#---------------------------------
# Ktor Rules
#---------------------------------

-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# OkHttp (used by Ktor OkHttp engine)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

#---------------------------------
# Room Database Rules
#---------------------------------

# Keep Room entity classes and their fields
-keep class com.dush1729.cfseeker.data.local.entity.** { *; }
-keepclassmembers class com.dush1729.cfseeker.data.local.entity.** { *; }

# Keep Room DAO interfaces
-keep interface com.dush1729.cfseeker.data.local.dao.** { *; }

# Room uses reflection for entity instantiation
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Room KMP generated database constructors
-keep class * extends androidx.room.RoomDatabase { <init>(); }

#---------------------------------
# Kotlin Specific Rules
#---------------------------------

# Keep Kotlin Metadata for reflection
-keepattributes RuntimeVisibleAnnotations

# Keep data class component functions and copy method
-keepclassmembers class com.dush1729.cfseeker.data.local.entity.** {
    public <init>(...);
    public ** component*();
    public ** copy(...);
}

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

#---------------------------------
# Koin Rules
#---------------------------------

-dontwarn org.koin.**
-keep class org.koin.** { *; }

#---------------------------------
# Firebase Rules
#---------------------------------

# Firebase Crashlytics
-keepattributes *Annotation*
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }

#---------------------------------
# Coil Image Loading
#---------------------------------

-dontwarn coil.**
-keep class coil.** { *; }

#---------------------------------
# WorkManager Rules
#---------------------------------

-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

#---------------------------------
# DataStore Rules
#---------------------------------

-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

#---------------------------------
# Compose Rules
#---------------------------------

# Compose compiler handles most of this, but keeping for safety
-dontwarn androidx.compose.**

#---------------------------------
# General Android Rules
#---------------------------------

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep BuildConfig
-keep class com.dush1729.cfseeker.BuildConfig { *; }

#---------------------------------
# Optimization settings
#---------------------------------

# Allow R8 to perform more aggressive optimizations
-allowaccessmodification
-repackageclasses ''

# Remove unused code paths
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
