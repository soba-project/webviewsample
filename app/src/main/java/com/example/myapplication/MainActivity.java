package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    //UA変更　AndroidのChrome
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.0.3; SC-02C Build/IML74K) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.58 Mobile Safari/537.31";
    private static final String URL = "https://update.soba-project.com/sfc/cm/call.html";
    private WebView mWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }

    /*
    パーミッション問い合わせしないとカメラとマイクが使えない。
     */
    private void checkPermission() {
        int permissionAll = 1;
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, permissionAll);
        } else {
            createWebView();
        }
    }

    private boolean hasPermissions(Context context, @org.jetbrains.annotations.NotNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 全ての必要なパーミッションが許可されたかチェック
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            createWebView();
        } else {
            finish();
        }
    }

    private void createWebView() {
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webview);
        createWebSettings();
        //webview内でページ遷移させるため
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });

        // ハードウェアアクセラレーション　HTML5対応のため
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mWebView.loadUrl(URL);
    }

    private void createWebSettings() {
        WebSettings webSettings = mWebView.getSettings();
        //UA
        webSettings.setUserAgentString(USER_AGENT);
        //javascript有効
        webSettings.setJavaScriptEnabled(true);
        //ズーム可能
        webSettings.setUseWideViewPort(true);
        // This forces ChromeClient enabled.
        webSettings.setSupportMultipleWindows(true);
        //subscriberの自動再生を許す。
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        //表示の最適化
        webSettings.setLoadWithOverviewMode(true);
    }

    //戻るボタン操作で前ページ戻れるようにしている
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //カメラが動いていたら停止させる。
        mWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
    }
}
