package com.yundesign.videoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TestActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        webView = findViewById(R.id.webview);
        webView.loadUrl("https://blog.csdn.net/bjstyle/article/details/45073503");
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
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
}
