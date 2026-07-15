# kotlinx.serialization: the compiler plugin generates a $$serializer companion per
# @Serializable class that Room/Retrofit/Supabase resolve at runtime via serializer(...).
# R8 strips it as "unused" unless kept explicitly. Rules per the official guide:
# https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class com.carlosarancibia.playfit.**$$serializer {
    *** Companion;
    *** INSTANCE;
    **serializer(...);
}
-keepclassmembers class com.carlosarancibia.playfit.** {
    *** Companion;
}
-keepclasseswithmembers class com.carlosarancibia.playfit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit response/request bodies and Supabase payloads are plain data classes decoded via
# kotlinx.serialization reflection on their properties; keep field names so JSON keys still match.
-keepclassmembers,allowobfuscation class com.carlosarancibia.playfit.data.remote.** {
    <fields>;
}
-keepclassmembers,allowobfuscation class com.carlosarancibia.playfit.data.local.** {
    <fields>;
}
