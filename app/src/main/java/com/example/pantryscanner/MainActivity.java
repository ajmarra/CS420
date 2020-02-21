package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button tempButton;
    Button recipeButton;
    Button pantryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempButton = findViewById(R.id.tempButton);

        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(i);
            }
        });

        recipeButton = findViewById(R.id.mainRecipeButton);
        recipeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i_recipe = new Intent(MainActivity.this, RecipeActivity.class);
                startActivity(i_recipe);
            }
        });

        pantryButton = findViewById(R.id.pantryButton);
        pantryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i_pantry = new Intent(MainActivity.this, PantryActivity.class);
                startActivity(i_pantry);
            }
        });
    }
}
