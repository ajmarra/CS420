package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PantryActivity extends AppCompatActivity {
    Button recipeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        recipeButton = findViewById(R.id.recipeButton);
        recipeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String url = constructUrl(getIngredients());

                Intent recipe_intent = new Intent(PantryActivity.this, RecipeActivity.class);
                recipe_intent.putExtra("RECIPE_URL", url);
                startActivity(recipe_intent);
            }
        });
    }

    private String[] getIngredients() {
        String[] ingredients = {"chicken"};
        return ingredients;
    }

    private String constructUrl(String[] ingredients) {
        String prefix = "https://www.allrecipes.com/search/results/?ingIncl=";
        String postfix = "&sort=re";
        String url = prefix + ingredients[0] + postfix;
        return url;
    }
}
