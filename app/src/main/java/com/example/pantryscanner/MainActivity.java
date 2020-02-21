package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button tempButton;
    Button recipeButton;

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

        recipeButton = findViewById(R.id.recipeButton);
        recipeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i_recipe = new Intent(MainActivity.this, RecipeActivity.class);
                startActivity(i_recipe);
            }
        });
    }
}
