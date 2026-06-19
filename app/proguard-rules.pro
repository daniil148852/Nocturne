# Add project specific ProGuard rules here.
# Keep kotlinx.serialization companions & annotations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.nocturne.**$$serializer { *; }
-keepclassmembers class com.nocturne.** {
    *** Companion;
}
-keepclasseswithmembers class com.nocturne.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Retrofit
-keepattributes Signature, Exceptions
-keepclasseswithmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }
# OkHttp SSE
-dontwarn okhttp3.internal.sse.**
# Mistral DTOs (defensive keep)
-keep class com.nocturne.data.api.dto.** { *; }
