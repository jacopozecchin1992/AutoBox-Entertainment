package it.autobox.entertainment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 100;
    private WebView webView;
    private GeolocationPermissions.Callback pendingGeolocation;
    private String pendingOrigin;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8,17,28));
        getWindow().setNavigationBarColor(Color.rgb(8,17,28));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8,17,28));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        // Bridge allows only the two saved destinations to be shared with Android Auto.
        webView.addJavascriptInterface(new DestinationBridge(), "AutoBoxBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternal(request.getUrl().toString());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternal(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    pendingGeolocation = callback;
                    pendingOrigin = origin;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                }
            }
        });
        setContentView(webView);
        try (InputStream input = getAssets().open("autobox.html"); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = input.read(buf)) != -1) bytes.write(buf, 0, n);
            String html = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            webView.loadDataWithBaseURL("https://autobox.local/", html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Toast.makeText(this, "Impossibile caricare AutoBox: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class DestinationBridge {
        private boolean isAllowed(String key) {
            return "place_home".equals(key) || "place_work".equals(key);
        }
        @JavascriptInterface public String getPlace(String key) {
            if (!isAllowed(key)) return "";
            return getSharedPreferences("autobox_prefs", MODE_PRIVATE).getString(key, "");
        }
        @JavascriptInterface public void savePlace(String key, String value) {
            if (!isAllowed(key)) return;
            String safe = value == null ? "" : value.trim();
            if (safe.length() > 300) safe = safe.substring(0, 300);
            getSharedPreferences("autobox_prefs", MODE_PRIVATE).edit().putString(key, safe).apply();
        }
    }

    private boolean openExternal(String url) {
        if (url.startsWith("https://autobox.local/")) return false;
        if (!(url.startsWith("https://") || url.startsWith("http://") || url.startsWith("tel:") || url.startsWith("sms:"))) return true;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nessuna app disponibile per questo collegamento", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST && pendingGeolocation != null) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            pendingGeolocation.invoke(pendingOrigin, granted, false);
            pendingGeolocation = null;
            pendingOrigin = null;
        }
    }
    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override public void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
