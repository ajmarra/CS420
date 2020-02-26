package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        WebView recipeWebview = (WebView) findViewById(R.id.recipeWebview);

//        recipeWebview.getSettings().setLoadWithOverviewMode(true); // TODO: Do we need this?
//        recipeWebview.getSettings().setUseWideViewPort(true); // TODO: Do we need this?
        recipeWebview.getSettings().setJavaScriptEnabled(true); // Apparently this is required, the page had issues loading until I added it
        recipeWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        String url = getIntent().getStringExtra("RECIPE_URL");
        recipeWebview.loadUrl(url);

    }
}
