package com.joaquimley.smsparsing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BLSViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blsview);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        WebView view = (WebView) findViewById(R.id.webView2);
        view.setWebViewClient(new WebViewClient());
        WebSettings webSettings = view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        view.loadUrl("https://algeria.blsspainvisa.com/book_appointment.php");

    }

    public void startInterval(View view) {
    }

    public void getCode(View view) {
    }

    public void BackToMain(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
