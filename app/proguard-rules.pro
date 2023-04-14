
#-----------------混淆配置设定------------------------------------------------------------------------
-optimizationpasses 5                                                       #指定代码压缩级别
-dontusemixedcaseclassnames                                                 #混淆时不会产生形形色色的类名
-dontskipnonpubliclibraryclasses                                            #指定不忽略非公共类库
-dontpreverify                                                              #不预校验，如果需要预校验，是-dontoptimize
-ignorewarnings                                                             #屏蔽警告
-verbose                                                                    #混淆时记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    #优化


-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留Parcelable序列化的类不能被混淆
-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable 序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   !static !transient <fields>;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

# 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 对于带有回调函数onXXEvent的，不能混淆
-keepclassmembers class * {
    void *(**On*Event);
}

#-----------------不需要混淆系统组件等-------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application {*;}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get* ();
}
-keep public class * extends android.view.animation.Animation {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}

# 保留Activity中的方法参数是view的方法，
# 从而我们在layout里面编写onClick就不会影响
-keepclassmembers class * extends android.app.Activity {
    public void * (android.view.View);
}

#-----------------不需要混淆第三方类库------------------------------------------------------------------
-keep public class javax.**

#过滤android.support.v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-dontwarn android.support.v7.**
-keep class android.support.v7.** {*;}
-keep interface android.support.v7.app.** {*;}
-keep class * extends android.support.v7.**

#过滤commons-httpclient-3.1.jar
-keep class org.apache.**{*;}
-keep interface org.apache.** {*;}

#gson
-dontwarn com.google.json.**
-keep class com.google.json.** {*;}
-keep interface com.google.json.** {*;}

#okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep interface com.squareup.okhttp.** { *; }
-dontwarn okio.**
-keep class okio.** {*;}
-keep interface okio.** {*;}

#glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.** {*;}
-keep interface com.bumptech.glide.** {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#bga-banner
-dontwarn cn.bingoogolapple.bgabanner.**
-keep class cn.bingoogolapple.bgabanner.** {*;}

#httpclient
-keep class android.net.compatibility.** {*;}
-keep class android.net.http.** {*;}
-keep interface android.net.http.** {*;}
-keep class com.android.internal.http.multipart.** {*;}
-keep interface com.android.internal.http.multipart.** {*;}

-keep class org.slf4j.** {*;}
-keep interface org.slf4j.** {*;}

-keep class com.pax.market.api.sdk.** {*;}
-keep interface com.pax.market.api.sdk.** {*;}

-dontwarn com.pax.eemv.**
-keep class com.pax.eemv.** {*;}
-keep interface com.pax.eemv.** {*;}

-dontwarn com.pax.gl.**
-keep class com.pax.gl.** {*;}
-keep interface com.pax.gl.** {*;}

-dontwarn com.pax.dal.**
-keep class com.pax.dal.** {*;}
-keep interface com.pax.dal.** {*;}
-dontwarn com.pax.neptunelite.**
-keep class com.pax.neptuenlite.** {*;}
-keep interface com.pax.neptuenlite.** {*;}
-keep class com.pax.neptuneliteapi.** {*;}
-keep interface com.pax.neptuneliteapi.** {*;}

-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** {*;}
-keep interface net.sqlcipher.** {*;}

-dontwarn com.google.zxing.**
-keep class com.google.zxing.** {*;}
-keep interface com.google.zxing.** {*;}

-dontwarn com.pax.appstore.**
-keep class com.pax.appstore.** {*;}
-keep interface com.pax.appstore.** {*;}


#过滤掉自己编写的实体类
-keep class com.pax.pay.trans.pack.** {*;}
-keep class com.pax.abl.core.ATransaction {*;}
-keep class com.pax.abl.core.AAction {*;}
-keep class * extends com.pax.abl.core.ATransaction {*;}
-keep class * extends com.pax.abl.core.AAction {*;}
-keep class com.pax.pay.service.dto.** {*;}
-keep class * extends com.pax.gl.db.IDb$AEntityBase {*;}
-keep class * implements com.pax.eemv.IEmvContactlessListener {*;}
-keep class * implements com.pax.eemv.IEmvDeviceListener {*;}
-keep class * implements com.pax.eemv.IEmvListener {*;}

-keep class com.pax.eemv.** {*;}
-keep class * extends com.pax.eemv.** {*;}
-keep class * implements com.pax.eemv.** {*;}
-keep class com.pax.jemv.clcommon.** {*;}
-keep class * extends com.pax.jemv.clcommon.** {*;}
-keep class * implements com.pax.jemv.clcommon.** {*;}
-keep class com.pax.jemv.amex.** {*;}
-keep class * extends com.pax.jemv.amex.** {*;}
-keep class * implements com.pax.jemv.amex.** {*;}
-keep class com.pax.jemv.device.** {*;}
-keep class * extends com.pax.jemv.device.** {*;}
-keep class * implements com.pax.jemv.device.** {*;}
-keep class com.pax.jemv.emv.** {*;}
-keep class * extends com.pax.jemv.emv.** {*;}
-keep class * implements com.pax.jemv.emv.** {*;}
-keep class com.pax.jemv.entrypoint.** {*;}
-keep class * extends com.pax.jemv.entrypoint.** {*;}
-keep class * implements com.pax.jemv.entrypoint.** {*;}
-keep class com.pax.jemv.paypass.** {*;}
-keep class * extends com.pax.jemv.paypass.** {*;}
-keep class * implements com.pax.jemv.paypass.** {*;}
-keep class com.pax.jemv.qpboc.** {*;}
-keep class * extends com.pax.jemv.qpboc.** {*;}
-keep class * implements com.pax.jemv.qpboc.** {*;}
-keep class com.pax.jemv.paywave.** {*;}
-keep class * extends com.pax.jemv.paywave.** {*;}
-keep class * implements com.pax.jemv.paywave.** {*;}



#----------------保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在------------------------------------
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
