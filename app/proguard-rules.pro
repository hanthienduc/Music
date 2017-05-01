# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.mnml.music.adapters.*
-keep class com.mnml.music.models.*
-keep class com.mnml.music.service.*
-keep class com.mnml.music.task.*
-keep class com.mnml.music.ui.*
-keep class com.mnml.music.utils.*
-keep class com.mnml.music.utils.glide.*
-keep class com.boswelja.lastfm.*
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*
-keep class .R
-keep class **.R$* {
    <fields>;
}
-keep class com.android.vending.billing.**

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn okio.**