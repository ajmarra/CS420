package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        WebView recipeWebview = (WebView) findViewById(R.id.recipeWebview);

        // Sets up a webview client to handle SSL errors
        recipeWebview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                Toast.makeText(RecipeActivity.this, "Recipe Page Unsafe, Request A Different Recipe", Toast.LENGTH_LONG).show();
                handler.cancel();
                Intent reverse_intent = new Intent(RecipeActivity.this, PantryActivity.class);
                startActivity(reverse_intent);
            }
        });

        // JavaScript is enabled because it is required for the recipe pages to load. We looked
        // into implementing XSS attack protection to mitigate security vulnerabilities, for
        // example the OWASP Project (https://www2.owasp.org/owasp-java-encoder/), but decided it
        // was beyond the scope of this class.
        recipeWebview.getSettings().setJavaScriptEnabled(true);

        //This is an attempt to stop pop-up ads
        recipeWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        // Load the url passed by the intent in the WebView
        String url = getIntent().getStringExtra("RECIPE_URL");
        recipeWebview.loadUrl(url);
    }
}
