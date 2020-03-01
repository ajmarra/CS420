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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        tv.setText(" Check Box ");
        tv.setTextColor(Color.WHITE);
        tbrow0.addView(tv);
        TextView tv1 = new TextView(this);
        tv1.setText(" Item Name ");
        tv1.setTextColor(Color.WHITE);
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
                                checkBox.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        // If it's been checked the it adds it to the array
                                        if (((CheckBox) v).isChecked()) {
                                            toSearch = ArrayUtils.appendToArray(toSearch, t2v.getText().toString());
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
                                        System.out.println(ArrayUtils.toArrayList(toSearch));
                                    }
                                });
                                tbrow.addView(checkBox);
                                tbrow.addView(t2v);
                                //If edit button is clicked then opens a text field with that item name
                                ImageButton editBtn = new ImageButton(PantryActivity.this);
                                editBtn.setImageResource(R.drawable.edit);
                                editBtn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        submitButton.setVisibility(View.VISIBLE);
                                        editText.setVisibility(View.VISIBLE);
                                        editText.setText(doc.getString("name"));
                                        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("name", editText.getText().toString());
                                                db.collection("pantry").document(doc.getId())
                                                        .set(data);
                                                t2v.setText(editText.getText().toString());
                                                // Text field and button disappear when done.
                                                submitButton.setVisibility(View.GONE);
                                                editText.setVisibility(View.GONE);
                                                Toast.makeText(PantryActivity.this, "Item Updated", Toast.LENGTH_LONG).show();
                                            }
                                        });
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
                            Log.w("PantryActivity", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void load_recipe_page(String recipe_url) {
        Intent recipe_intent = new Intent(PantryActivity.this, RecipeActivity.class);
        recipe_intent.putExtra("RECIPE_URL", recipe_url);
        startActivity(recipe_intent);
    }

    private String choose_recipe(List<String> all_recipes, String mode) {
        String chosen_recipe_url;
        if (mode.equals("random")) {
            Random rand = new Random();
            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
        } else {
            System.out.println("Only random url selection currently implemented. Defaulting to random.");
            Random rand = new Random();
            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
        }
        return chosen_recipe_url;
    }

    private boolean is_recipe_url(String possible_recipe) {
        String url_template = "https://www.allrecipes.com/recipe/";
        return url_template.regionMatches(0, possible_recipe, 0, 34);
    }

    private List<String> parse_webpage(Document document) {
        Elements links = document.select("a[href]");
        List<String> recipe_urls = new ArrayList<String>();
        for (Element link : links) {
            // get the value from the href attribute
            if (is_recipe_url(link.attr("href"))) {
                System.out.println("link: " + link.attr("href"));
                recipe_urls.add(link.attr("href"));
            }
        }
        return recipe_urls;
    }

    public class Webscraper implements Runnable {
        String scrape_url;

        public Webscraper(String input_url) {
            scrape_url = input_url;
        }
        public void webscrape() {
            try {
                // Plug this into a background thread (An asynchronous task)
                Document document = Jsoup.connect(scrape_url).get();
                List<String> recipe_urls = parse_webpage(document);
                String mode = "random";
                String chosen_recipe_url = choose_recipe(recipe_urls, mode);
                load_recipe_page(chosen_recipe_url);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            webscrape();
        }
    }

    public class Invoker implements Executor {
        @Override
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    public void get_recipe_and_load_page(String search_url) {
        Executor executor = new Invoker();
        executor.execute(new Webscraper(search_url));
    }

    private String[] getIngredients() {
        // Hard coded for testing
        String[] ingredients = {"pork", "peppers", "rice", "onions"};
        if (toSearch.length == 0) Toast.makeText(PantryActivity.this, "Select an item!", Toast.LENGTH_LONG).show();
        return toSearch;
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

    private void invoke_recipe_pipeline() {
        String search_url = constructSearchUrl(getIngredients());
        get_recipe_and_load_page(search_url);
    }
}