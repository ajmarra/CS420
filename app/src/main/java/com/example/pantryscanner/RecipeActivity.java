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

//        recipeWebview.getSettings().setLoadWithOverviewMode(true); // TODO: Add reason for this line
//        recipeWebview.getSettings().setUseWideViewPort(true); // TODO: Add reason for this line
        recipeWebview.getSettings().setJavaScriptEnabled(true); // Apparently this is required, the page had issues loading until I added it
        recipeWebview.loadUrl("https://www.allrecipes.com/recipe/260455/chicken-kebabs/");

    }
}
