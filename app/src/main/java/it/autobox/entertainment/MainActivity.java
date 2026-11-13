package it.autobox.entertainment;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.speech.RecognizerIntent;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 100;
    private static final int VOICE_REQUEST = 101;
    private WebView webView;
    private GeolocationPermissions.Callback pendingGeolocation;
    private String pendingOrigin;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8, 17, 28));
        getWindow().setNavigationBarColor(Color.rgb(8, 17, 28));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8, 17, 28));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        // Only bundled HTML is loaded; the bridge exposes a fixed list of app shortcuts.
        webView.addJavascriptInterface(new DestinationBridge(), "AutoBoxBridge");
        webView.addJavascriptInterface(new NativeShortcuts(), "AutoBoxNative");
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
                    requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_REQUEST);
                }
            }
        });
        setContentView(webView);
        try (InputStream input = getAssets().open("autobox.html");
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = input.read(buf)) != -1) bytes.write(buf, 0, n);
            String html = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            webView.loadDataWithBaseURL("https://autobox.local/", html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Toast.makeText(this, "Impossibile caricare AutoBox", Toast.LENGTH_LONG).show();
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

    public class NativeShortcuts {
        @JavascriptInterface public void openApp(String key) {
            runOnUiThread(() -> launchShortcut(key));
        }
        @JavascriptInterface public void navigate(String address) {
            runOnUiThread(() -> navigateWaze(address));
        }
    }

    private void message(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean launchInstalled(String packageName) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) return false;
        try {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void openInstalled(String displayName, String... packages) {
        for (String pkg : packages) {
            if (launchInstalled(pkg)) return;
        }
        message(displayName + " non installata. Installa l'app ufficiale sul telefono.");
    }

    private void launchShortcut(String key) {
        if (key == null) return;
        switch (key) {
            case "maps":
                openInstalled("Google Maps", "com.google.android.apps.maps");
                break;
            case "waze":
                openInstalled("Waze", "com.waze");
                break;
            case "spotify":
                openInstalled("Spotify", "com.spotify.music");
                break;
            case "local_music":
                startActivity(new Intent(this, AudioLibraryActivity.class));
                break;
            case "audio_play":
                sendAudioCommand(AutoBoxMediaService.ACTION_PLAY);
                break;
            case "audio_pause":
                sendAudioCommand(AutoBoxMediaService.ACTION_PAUSE);
                break;
            case "audio_next":
                sendAudioCommand(AutoBoxMediaService.ACTION_NEXT);
                break;
            case "audio_previous":
                sendAudioCommand(AutoBoxMediaService.ACTION_PREVIOUS);
                break;
            case "ytmusic":
                openInstalled("YouTube Music", "com.google.android.apps.youtube.music");
                break;
            case "whatsapp":
                openInstalled("WhatsApp", "com.whatsapp", "com.whatsapp.w4b");
                break;
            case "sms":
                String smsDefault = Telephony.Sms.getDefaultSmsPackage(this);
                if (smsDefault != null && launchInstalled(smsDefault)) return;
                openInstalled("Messaggi", "com.google.android.apps.messaging", "com.samsung.android.messaging");
                break;
            case "calls":
                try {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
                } catch (Exception e) {
                    message("Nessuna app Telefono disponibile.");
                }
                break;
            case "youtube":
                openInstalled("YouTube", "com.google.android.youtube");
                break;
            case "netflix":
                openInstalled("Netflix", "com.netflix.mediaclient");
                break;
            case "disney":
                openInstalled("Disney+", "com.disney.disneyplus");
                break;
            case "prime":
                openInstalled("Prime Video", "com.amazon.avod.thirdpartyclient");
                break;
            case "voice":
                openVoice();
                break;
            default:
                message("Scorciatoia non disponibile.");
        }
    }

    private void sendAudioCommand(String action) {
        try {
            Intent intent = new Intent(this, AutoBoxMediaService.class).setAction(action);
            startService(intent);
        } catch (Exception e) {
            message("Impossibile controllare la musica AutoBox. Apri la libreria musicale.");
        }
    }

    private void navigateWaze(String address) {
        if (address == null || address.trim().isEmpty()) {
            message("Inserisci una destinazione.");
            return;
        }
        // Waze deep-link documented by Google: q is the URL-encoded search,
        // navigate=yes starts guidance; package restriction prevents browser fallback.
        Uri target = Uri.parse("waze://?q=" + Uri.encode(address.trim()) +
                "&navigate=yes");
        Intent navigation = new Intent(Intent.ACTION_VIEW, target);
        navigation.setPackage("com.waze");
        try {
            startActivity(navigation);
        } catch (ActivityNotFoundException e) {
            message("Installa o aggiorna Waze per avviare il percorso.");
        } catch (Exception e) {
            message("Impossibile aprire la navigazione in Waze.");
        }
    }

    private void openVoice() {
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ITALIAN.toLanguageTag());
        voice.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dove vuoi andare?");
        try {
            startActivityForResult(voice, VOICE_REQUEST);
        } catch (Exception e) {
            message("Installa un'app di riconoscimento vocale.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != VOICE_REQUEST || resultCode != RESULT_OK || data == null) return;
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) return;
        String original = results.get(0).trim();
        String command = original.toLowerCase(Locale.ITALIAN);
        if (command.startsWith("naviga a ")) navigateWaze(original.substring(9));
        else if (command.startsWith("vai a ")) navigateWaze(original.substring(6));
        else if (command.contains("spotify")) launchShortcut("spotify");
        else if (command.contains("waze")) launchShortcut("waze");
        else if (command.contains("whatsapp")) launchShortcut("whatsapp");
        else if (command.contains("chiama")) launchShortcut("calls");
        else message("Comando non riconosciuto: " + original);
    }

    private boolean openExternal(String url) {
        if (url.startsWith("https://autobox.local/")) return false;
        // Do not turn Android shortcuts into website fallbacks.
        if (url.startsWith("https://") || url.startsWith("http://")) {
            message("Usa le scorciatoie AutoBox per aprire le app installate.");
            return true;
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
