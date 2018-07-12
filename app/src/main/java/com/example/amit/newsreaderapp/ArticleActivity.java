package com.example.amit.newsreaderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        mWebView=findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        //mWebView.loadData(getIntent().getStringExtra("html"),"text/html","UTF-8");


        // if  network is offline than load basic html, otherwise load the whole webpage

        if(!getIntent().getBooleanExtra("network",false))
        mWebView.loadDataWithBaseURL(getIntent().getStringExtra("url"),getIntent().getStringExtra("html"),"text/html","UTF-8","");
        else
        mWebView.loadUrl(getIntent().getStringExtra("url"));
    }
}
