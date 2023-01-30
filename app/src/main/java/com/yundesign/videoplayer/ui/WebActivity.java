package com.yundesign.videoplayer.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Process;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.emp.xdcommon.android.log.Log;
import com.emp.xdcommon.common.utils.ToastUtil;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.ConfigManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WebActivity extends BaseActivity {

    //    private static Object TAG;
    private static String TAG = "WebActivity";
    private WebView webView;
    private ConfigManager configManager;
    private int currentPos;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(Command command) {
        if (command.getCmd() == 12) {
            String content = command.getContent();
            if (content == null)
                return;
            switch (content) {
                case "next":             //下一个网站
                    nextWeb();
                    break;
                case "prev":             //上一个网站
                    prevWeb();
                    break;
                case "first":             //第一个网站
                    firstWeb();
                    break;
                case "last":             //最后一个网站
                    lastWeb();
                    break;
                default:
                    openWeb(content);
                    break;
            }
        }
    }

    private void lastWeb() {
        currentPos = ConfigManager.getInstance().getWebList().size() - 1;
        openWeb(configManager.getWebList().get(currentPos));
    }

    private void firstWeb() {
        currentPos = 0;
        openWeb(configManager.getWebList().get(currentPos));
    }

    private void prevWeb() {
        if (currentPos == 0) {
            ToastUtil.shortTips("已经是第一个了");
            return;
        }
        currentPos--;
        openWeb(configManager.getWebList().get(currentPos));
    }

    private void nextWeb() {
        if (currentPos == configManager.getWebList().size() - 1) {
            ToastUtil.shortTips("已经是最后一个了");
            return;
        }
        currentPos++;
        openWeb(configManager.getWebList().get(currentPos));
    }

    private void openWeb(String content) {
        if (webView != null)
            webView.loadUrl(content);
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(content));
            startActivity(intent);
        }
    }

//    public void hookWebView(){
//        int sdkInt = Build.VERSION.SDK_INT;
//        try {
//            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
//            Field field = factoryClass.getDeclaredField("sProviderInstance");
//            field.setAccessible(true);
//            Object sProviderInstance = field.get(null);
//            if (sProviderInstance != null) {
//                Log.i(TAG,"sProviderInstance isn't null");
//                return;
//            }
//
//            Method getProviderClassMethod;
//            if (sdkInt > 22) {
//                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
//            } else if (sdkInt == 22) {
//                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
//            } else {
//                Log.i(TAG,"Don't need to Hook WebView");
//                return;
//            }
//            getProviderClassMethod.setAccessible(true);
//            Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
//            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
//            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
//            delegateConstructor.setAccessible(true);
//            if(sdkInt < 26){//低于Android O版本
//                Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
//                if (providerConstructor != null) {
//                    providerConstructor.setAccessible(true);
//                    sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
//                }
//            } else {
//                Field chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD");
//                chromiumMethodName.setAccessible(true);
//                String chromiumMethodNameStr = (String)chromiumMethodName.get(null);
//                if (chromiumMethodNameStr == null) {
//                    chromiumMethodNameStr = "create";
//                }
//                Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
//                if (staticFactory!=null){
//                    sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
//                }
//            }
//
//            if (sProviderInstance != null){
//                field.set("sProviderInstance", sProviderInstance);
//                Log.i(TAG,"Hook success!");
//            } else {
//                Log.i(TAG,"Hook failed!");
//            }
//        } catch (Throwable e) {
//            Log.w(TAG,e);
//        }
//    }

    public static void hookWebView() {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                com.emp.xdcommon.android.log.Log.e(TAG, "sProviderInstance isn't null");
                return;
            }
            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                com.emp.xdcommon.android.log.Log.i(TAG, "Don't need to Hook WebView");
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
                com.emp.xdcommon.android.log.Log.e(TAG, "sProviderInstance:{}" + sProviderInstance);
                field.set("sProviderInstance", sProviderInstance);
            }
            Log.e(TAG, "hook done");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView() {
        if (Process.myUid() != Process.SYSTEM_UID) {
            webView = findViewById(R.id.webview);
            WebSettings ws = webView.getSettings();
            ws.setJavaScriptEnabled(true);
            ws.setJavaScriptCanOpenWindowsAutomatically(true);
            ws.setUseWideViewPort(true);
            ws.setAppCacheEnabled(true);
            ws.setDomStorageEnabled(true);
            ws.setLoadWithOverviewMode(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
//        } else {
//            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//        }
            //ws.setCacheMode(mode);
//        ws.setSupportZoom(true);
//        ws.setBuiltInZoomControls(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();//忽略证书的错误继续Load页面内容，不会显示空白页面
                }
            });
            webView.setWebChromeClient(new WebChromeClient());
        }
        configManager = ConfigManager.getInstance();
        if (configManager.getWebList().size() > 0)
            openWeb(configManager.getWebList().get(0));
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected int getContentLayout() {
        if (Process.myUid() == Process.SYSTEM_UID) {
            hookWebView();
            return R.layout.noweblay;
        }
        return R.layout.activity_web;
    }
}
