-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class com.example.yanderem3.model.** { *; }
-keep class com.example.yanderem3.data.remote.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-keep class coil.** { *; }
-keep class * extends coil.decode.Decoder { <init>(...); }
-keep class * extends coil.fetch.Fetcher { <init>(...); }

-keep class androidx.room.** { *; }
-keep @androidx.room.* class * { *; }
