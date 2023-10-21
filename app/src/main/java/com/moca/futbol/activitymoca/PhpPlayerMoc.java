package com.moca.futbol.activitymoca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.moca.futbol.R;
import com.monstertechno.adblocker.AdBlockerWebView;


public class PhpPlayerMoc extends AppCompatActivity {


    String streamUrl ;
    private WebView mywebView;
    private ProgressBar myprogressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.pobr_external_player);



        Intent intent = getIntent();
       streamUrl = intent.getStringExtra("EXTRA_TEXT");



        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d("--->Network", "onCreate: is 4G");
            }
        }

        if (streamUrl == null) {
            errorDialog();
        }

        new AdBlockerWebView.init(this);

        mywebView = findViewById(R.id.video);
        myprogressBar = findViewById(R.id.load);
        mywebView.setBackgroundColor(Color.TRANSPARENT);
        mywebView.setFocusableInTouchMode(false);
        mywebView.setFocusable(false);
        mywebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mywebView.getSettings().setJavaScriptEnabled(true);
        mywebView.getSettings().setAllowContentAccess(true);
        mywebView.getSettings().setAllowFileAccess(true);
        mywebView.getSettings().setDomStorageEnabled(true);
        mywebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

//        mywebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        mywebView.setWebViewClient(new WebViewClient() {

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // List of URLs or patterns to block
                String[] blockedUrls = {
                        "/script/resource-v31.js",
                        "//s10.histats.com/js15_as.js",
                        "//whos.amung.us/",
                        "https://ad.contentango.online/",
                        "//ad.contentango",
                        "//youradexchange.com",
                        "https://abolishstand.net/deb.js",
                        "https://assets.alicdn",
                        "histats.com"


                };

                for (String blockedUrl : blockedUrls) {
                    if (url.endsWith(blockedUrl) || url.contains(blockedUrl)) {
                        return new WebResourceResponse("text/plain", "utf-8", null);
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Inject JavaScript to Remove Unwanted Elements

                // 1. Remove/hide the ad banner
                view.loadUrl("javascript:(function() { " +
                        "document.getElementById('html1').style.display='none';" +
                        "})()");

                // 2. Remove any other unwanted elements or scripts. For instance, the following is just a sample to demonstrate how to remove a script by its source.
                // If you identify other scripts or elements to remove, you can add more lines like this.
                view.loadUrl("javascript:(function() { " +
                        "var scripts = document.getElementsByTagName('script');" +
                        "for (var i = 0; i < scripts.length; i++) { " +
                        "    if (scripts[i].src.includes('/script/resource-v31.js')) { " +
                        "        scripts[i].parentNode.removeChild(scripts[i]);" +
                        "    } " +
                        "}" +
                        "})()");
            }
        });


        mywebView.setWebChromeClient(new WebChromeClient() {

            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    myprogressBar.setVisibility(View.GONE);
                    mywebView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }

            @Override
            public void onHideCustomView() {
                ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }



            @Override
            public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846);
            }
        });
    mywebView.loadUrl(streamUrl);

    }

    public void errorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pobr_whop))
                .setCancelable(false)
                .setMessage(getString(R.string.msg_failed))
//              .setPositiveButton(getString(R.string.option_retry), (dialog, which) -> finish())
                .setNegativeButton(getString(R.string.option_no), (dialogInterface, i) -> finish())
                .show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mywebView != null) {
            mywebView.destroy();
        }
    }
}