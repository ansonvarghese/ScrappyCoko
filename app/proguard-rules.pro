# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\ms3\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

-dontwarn rx.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

-keep public enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keeppackagenames org.jsoup.nodes
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class android.support.v7.widget.SearchView { *; }

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

-keep class rx.internal.util.unsafe.** {
    *;
 }

-keep class com.myscrap.model
-keep class com.myscrap.model.ChatRoomResponse

-keep class *{
    private public *;
}

-ignorewarnings

-keep class com.myscrap.model
-keep class com.myscrap.model.Contact
-keep class com.myscrap.model.Contact.ContactData
-keep class com.myscrap.model.Favourite
-keep class com.myscrap.model.MyItem
-keep class com.myscrap.model.ChatRoomResponse
-keep class com.myscrap.model.ChatRoom
-keep class com.myscrap.model.Favourite.FavouriteData
-keep class com.myscrap.adapters.ContactsFragmentAdapter
-keep class com.myscrap.adapters.FavouriteFragmentAdapter
-keep class com.myscrap.model.** { *; }
-keep class *{
    private public *;
}

-keep class com.myscrap.model.ShakeFriend
-keep class *{
    private public *;
}

-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

 -keep public class * implements com.bumptech.glide.module.GlideModule
 -keep public class * extends com.bumptech.glide.module.AppGlideModule
 -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
   **[] $VALUES;
   public *;
 }


-keep class com.google.gson.** { *; }
-keep public class com.google.gson.** {public private protected *;}
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class retrofit.** { *; }
-keep class com.google.appengine.** { *; }
-keepattributes *Annotation*

-dontwarn retrofit.**
 -keep class retrofit.** { *; }
 -keepattributes Signature
 -keepattributes Exceptions
 -keepclasseswithmembers class * {
     @retrofit.http.* <methods>;
 }


 # Platform calls Class.forName on types which do not exist on Android to determine platform.
 -dontnote retrofit2.Platform
 # Platform used when running on Java 8 VMs. Will not be used at runtime.
 -dontwarn retrofit2.Platform$Java8
 # Retain generic type information for use by reflection by converters and adapters.
 -keepattributes Signature
 # Retain declared checked exceptions for use by a Proxy instance.
 -keepattributes Exceptions

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.view.menu.*MenuBuilder*, android.support.v7.** { *; }
-keep interface android.support.v7.* { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
