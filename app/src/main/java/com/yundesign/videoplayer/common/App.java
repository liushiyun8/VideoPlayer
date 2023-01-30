package com.yundesign.videoplayer.common;

import android.app.Application;
import android.os.Build;
import android.os.Process;

import com.emp.xdcommon.android.base.CrashHandler;
import com.emp.xdcommon.android.log.Log;
import com.emp.xdcommon.android.log.LogUtils;
import com.emp.xdcommon.common.utils.ToastUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class App extends Application {

    private static String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.logConfigure(this, AppConfig.LOGPATH);
        LogUtils.LOG2FILE = true;
        CrashHandler.getInstance().init(this, AppConfig.CRASHPATH);
        ToastUtil.init(this);
    }

    public static void hookWebView() {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                Log.e(TAG, "sProviderInstance isn't null");
                return;
            }
            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                Log.i(TAG,"Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> providerConstructor = providerClass.getConstructor(delegateClass);
            if (providerConstructor != null) {
                providerConstructor.setAccessible(true);
                Constructor<?> declaredConstructor = delegateClass.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                sProviderInstance = providerConstructor.newInstance(declaredConstructor.newInstance());
                Log.e(TAG, "sProviderInstance:{}" + sProviderInstance);
                field.set("sProviderInstance", sProviderInstance);
            }
            Log.e(TAG, "hook done");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
