package com.example.pantryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PantryActivity extends AppCompatActivity {
    private Button recipeButton, addItemButton, submitButton;
    private EditText editText;
    private FirebaseFirestore db;
    private TableLayout tble;
    private String[] toSearch = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        // Initializing database
        db = FirebaseFirestore.getInstance();

        // Makes the edit button and field invisible until needed
        submitButton = findViewById(R.id.submitBtn);
        editText = findViewById(R.id.editText);
        submitButton.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        // Switch to AddItemActivity
        addItemButton = findViewById(R.id.addItemAct);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(PantryActivity.this, AddItemActivity.class);
                startActivity(i);
            }
        } );

        recipeButton = findViewById(R.id.recipeButton);
        recipeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (ActivityCompat.checkSelfPermission(PantryActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PantryActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
                    } else {
                        invoke_recipe_pipeline();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        init();
    }

    // Populates table with pantry data
    private void init() {
        tble = findViewById(R.id.displayLayout);
        TableRow tbrow0 = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText("Check Box ");
        tv.setTextColor(Color.WHITE);
        tbrow0.addView(tv);
        TextView tv1 = new TextView(this);
        tv1.setText(" Item Name ");
        tv1.setTextColor(Color.WHITE);
        tv1.setGravity(Gravity.CENTER);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);;
        tv2.setText(" Edit Item ");
        tv2.setTextColor(Color.WHITE);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText(" Delete Item ");
        tv3.setTextColor(Color.WHITE);
        tbrow0.addView(tv3);
        tble.addView(tbrow0);
        // Gets all db entries
        db.collection("pantry")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final QueryDocumentSnapshot doc = document;
                                final TableRow tbrow = new TableRow(PantryActivity.this);
                                final CheckBox checkBox = new CheckBox(PantryActivity.this);
                                final TextView t2v = new TextView(PantryActivity.this);
                                t2v.setText(document.getString("name"));
                                t2v.setTextColor(Color.BLACK);
                                t2v.setGravity(Gravity.CENTER);
                                t2v.setMaxWidth(500);
                                checkBox.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        handleSelectedCheckBox(doc, v, t2v);
                                    }
                                });
                                tbrow.addView(checkBox);
                                tbrow.addView(t2v);
                                //If edit button is clicked then opens a text field with that item name
                                ImageButton editBtn = new ImageButton(PantryActivity.this);
                                editBtn.setImageResource(R.drawable.edit);
                                editBtn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        editItem(doc, t2v);
                                    }
                                });
                                tbrow.addView(editBtn);
                                ImageButton deleteBtn = new ImageButton(PantryActivity.this);
                                deleteBtn.setImageResource(R.drawable.delete);
                                deleteBtn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        db.collection("pantry").document(doc.getId()).delete();
                                        tble.removeView(tbrow);
                                        Toast.makeText(PantryActivity.this, "Item Deleted", Toast.LENGTH_LONG).show();
                                    }
                                });
                                tbrow.addView(deleteBtn);
                                tble.addView(tbrow);
                            }
                        } else {
                            Toast.makeText(PantryActivity.this, "Error getting documents", Toast.LENGTH_LONG).show();
                            Log.w("PantryActivity", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    // Adds or removes items from the toSearch list when items are checked or unchecked
    private void handleSelectedCheckBox(QueryDocumentSnapshot doc, View v, TextView tv) {
        // If it's been checked the it adds it to the array
        if (((CheckBox) v).isChecked()) {
            toSearch = ArrayUtils.appendToArray(toSearch, tv.getText().toString());
        }
        // If it's been unchecked it removes that item from the array
        else {
            String[] temp = new String[toSearch.length - 1];
            for (int i = 0, k = 0; i < toSearch.length; i++) {
                if (toSearch[i] == doc.getString("name")) {
                    continue;
                }
                temp[k++] = toSearch[i];
            }
            toSearch = temp;
        }
        System.out.println(ArrayUtils.toArrayList(toSearch).toString());
    }

    // Opens a field to edit an existing item in the database
    private void editItem (QueryDocumentSnapshot doc, TextView tv) {
        final TextView tv1 = tv;
        final QueryDocumentSnapshot doc1 = doc;
        submitButton.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        editText.setText(doc.getString("name"));
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", editText.getText().toString());
                db.collection("pantry").document(doc1.getId())
                        .set(data);
                tv1.setText(editText.getText().toString());
                // Text field and button disappear when done.
                submitButton.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                Toast.makeText(PantryActivity.this, "Item Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Passes the selected recipe url into an intent and loads the RecipeActivity
    private void load_recipe_page(String recipe_url) {
        Intent recipe_intent = new Intent(PantryActivity.this, RecipeActivity.class);
        recipe_intent.putExtra("RECIPE_URL", recipe_url);
        startActivity(recipe_intent);
    }

    // Selects one of the found recipes using the specified criteria
    private String choose_recipe(List<String> all_recipes, String mode) {
        String chosen_recipe_url;
        if (mode.equals("reviews")) {
            // TODO: Implement review based recipe selection if we continue developing the app
            Random rand = new Random();
            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
        } else {
            // Default to random recipe selection
            System.out.println("Only random url selection currently implemented. Defaulting to random.");
            Random rand = new Random();
            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
        }
        return chosen_recipe_url;
    }

    // Checks whether a found link is for a new recipe
    private boolean is_recipe_url(String possible_recipe) {
        String url_template = "https://www.allrecipes.com/recipe/";
        return url_template.regionMatches(0, possible_recipe, 0, 34);
    }
    // Grabs product names from the recipe search website
    private List<String> parse_recipe_webpage(Document document) {
        Elements links = document.select("a[href]");
        List<String> recipe_urls = new ArrayList<String>();
        for (Element link : links) {
            // get the value from the href attribute
            if (is_recipe_url(link.attr("href"))) {
                recipe_urls.add(link.attr("href"));
            }
        }
        return recipe_urls;
    }

    //  A callable class to pass into an asynchronous task with webscraping instructions
    public class RecipeCallable implements Callable<Void> {
        private String search_url;

        public RecipeCallable(String search_url) {
            this.search_url = search_url;
        }

        @Override
        public Void call() {
            try {
                // Plug this into a background thread (An asynchronous task)
                Document document = Jsoup.connect(this.search_url).get();
                List<String> recipe_urls = parse_recipe_webpage(document);
                if (recipe_urls.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No Recipes Found. Deselect Some Ingredients And Try Again", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    String mode = "random";
                    String chosen_recipe_url = choose_recipe(recipe_urls, mode);
                    load_recipe_page(chosen_recipe_url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Builds a url string by concatenating ingredients and commas
    private String constructSearchUrl() {
        String prefix = "https://www.allrecipes.com/search/results/?ingIncl=";
        String postfix = "&sort=re";
        String search_url = prefix;
        String new_ingredient;
        String[] list_of_words;
        for(int i = 0; i < toSearch.length; i++) {
            if (i != 0) {
                search_url += ',';
            }
            new_ingredient = toSearch[i].replaceAll("[^a-zA-Z]", " ");
            list_of_words = new_ingredient.split(" ");
            for(int j = 0; j < list_of_words.length; j++) {
                if (j != 0) {
                    search_url += ',';
                }
                search_url += list_of_words[j];
            }
        }
        search_url += postfix;
        return search_url;

    }

    // Suggests a tailored recipe and opens a webview with it
    private void invoke_recipe_pipeline() {
        if (toSearch.length > 0) {
            String search_url = constructSearchUrl();
            Executor executor = new Invoker();
            Callable recipe_scrape_call = new RecipeCallable(search_url);
            executor.execute(new Webscraper(recipe_scrape_call));
        }
        else {
            // At least one ingredient must be selected to suggest a recipe
            Toast.makeText(getApplicationContext(),"Select At Least One Ingredient",Toast.LENGTH_SHORT).show();
        }
    }
}