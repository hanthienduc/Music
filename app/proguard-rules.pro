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
-keep class com.dominionos.music.adapters.*
-keep class com.dominionos.music.items.*
-keep class com.dominionos.music.service.*
-keep class com.dominionos.music.task.*
-keep class com.dominionos.music.ui.*
-keep class com.dominionos.music.utils.*
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*
-keep class .R
-keep class **.R$* {
    <fields>;
}