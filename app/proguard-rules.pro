-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.davilex.impostor.**$$serializer { *; }
-keepclassmembers class com.davilex.impostor.** {
    *** Companion;
}
-keepclasseswithmembers class com.davilex.impostor.** {
    kotlinx.serialization.KSerializer serializer(...);
}
