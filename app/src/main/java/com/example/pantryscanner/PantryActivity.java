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

                String url = getRecipeURL();

                Intent recipe_intent = new Intent(PantryActivity.this, RecipeActivity.class);
                recipe_intent.putExtra("RECIPE_URL", url);
                startActivity(recipe_intent);
            }
        });
    }

    private String[] getIngredients() {
        String[] ingredients = {"sugar", "flour", "salt"};
        return ingredients;
    }

    private String constructSearchUrl(String[] ingredients) {
        String prefix = "https://www.allrecipes.com/search/results/?ingIncl=";
        String postfix = "&sort=re";
        String search_url = prefix;
        for(int i = 0; i < ingredients.length; i++) {
            if (i != 0) {
                search_url += ',';
            }
            search_url += ingredients[i];
        }

        search_url += postfix;
        return search_url;
    }

    // Gets all recipe urls from allrecipes.com containing the specified ingredients
    private String retrieveRecipeUrls(String search_url) {

        return search_url;
    }

    // Gets a single recipe url that contains the requested ingredients
    private String getRecipeURL() {

        String search_url = constructSearchUrl(getIngredients());

        String recipe_url = retrieveRecipeUrls(search_url);

        return recipe_url;
    }
}
