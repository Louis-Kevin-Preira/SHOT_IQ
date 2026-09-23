package com.fable.shotiq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

/**
 * SHOT IQ — enveloppe Android.
 *
 * Les fichiers de l'application vivent dans app/src/main/assets/www. Ils sont servis
 * par WebViewAssetLoader sous https://appassets.androidplatform.net/, et non en file://.
 * Cela donne un contexte sécurisé : localStorage garde les séances d'une mise à jour
 * à l'autre, la synthèse vocale et le verrouillage de l'écran fonctionnent.
 */
public class MainActivity extends Activity {

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Material_NoActionBar);   // on quitte l'écran de lancement
        super.onCreate(savedInstanceState);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);              // localStorage : l'historique des séances
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); // signaux sonores de fin de bloc
        s.setSupportZoom(false);
        s.setTextZoom(100);

        web.setBackgroundColor(0xFF0A1730);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setKeepScreenOn(false);

        // Sans WebChromeClient, la WebView ignore les dialogues et bloque
        // certaines lectures média. On le fournit même si l'application
        // n'utilise plus de boîtes natives.
        web.setWebChromeClient(new WebChromeClient());

        web.setWebViewClient(new WebViewClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String host = u.getHost();
                // les liens internes restent dans l'app, les liens vidéo partent au navigateur
                if (host != null && host.equals("appassets.androidplatform.net")) return false;
                try {
                    startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, u));
                } catch (Exception ignored) { }
                return true;
            }
        });

        setContentView(web);
        web.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
    }

    /**
     * Le bouton retour ferme d'abord ce qui est ouvert dans l'application :
     * une feuille, le mode mains libres, puis la séance en cours.
     */
    @Override
    public void onBackPressed() {
        web.evaluateJavascript(
                "(function(){try{return !!(window.__androidBack&&window.__androidBack());}catch(e){return false;}})()",
                value -> {
                    if (!"true".equals(value)) {
                        moveTaskToBack(true);   // on sort sans détruire l'activité
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (web != null) web.evaluateJavascript("try{window.dispatchEvent(new Event('apppause'))}catch(e){}", null);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
