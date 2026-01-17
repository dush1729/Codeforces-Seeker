# ProGuard rules for CFSeeker
# Generated to protect serialization and maintain functionality

#---------------------------------
# Preserve line numbers for debugging stack traces (Firebase Crashlytics)
#---------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#---------------------------------
# Gson Serialization Rules
#---------------------------------

# Preserve generic type information for Gson (critical for List<T>, Map<K,V>, etc.)
-keepattributes Signature

# Preserve annotations used by Gson
-keepattributes *Annotation*

# Keep Gson classes
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep @SerializedName annotation and its value
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all Codeforces API model classes (remote models with @SerializedName)
-keep class com.dush1729.cfseeker.data.remote.model.** { *; }
-keepclassmembers class com.dush1729.cfseeker.data.remote.model.** { *; }

#---------------------------------
# Retrofit Rules
#---------------------------------

# Keep Retrofit interfaces and their methods
-keep,allowobfuscation interface com.dush1729.cfseeker.data.remote.api.NetworkService
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Retrofit does reflection on generic parameters
-keepattributes Exceptions

# Keep Retrofit classes
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
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

#---------------------------------
# Kotlin Specific Rules
#---------------------------------

# Keep Kotlin Metadata for reflection
-keepattributes RuntimeVisibleAnnotations

# Keep data class component functions and copy method
-keepclassmembers class com.dush1729.cfseeker.data.remote.model.** {
    public <init>(...);
    public ** component*();
    public ** copy(...);
}

-keepclassmembers class com.dush1729.cfseeker.data.local.entity.** {
    public <init>(...);
    public ** component*();
    public ** copy(...);
}

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

#---------------------------------
# Hilt / Dagger Rules
#---------------------------------

# Hilt generates its own proguard rules, but these help ensure safety
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

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
