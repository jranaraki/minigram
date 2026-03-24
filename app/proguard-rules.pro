# Add project specific ProGuard rules here.
-keep class org.pytorch.executorch.** { *; }
-keep class * extends org.pytorch.executorch.Module
-dontwarn org.pytorch.executorch.**