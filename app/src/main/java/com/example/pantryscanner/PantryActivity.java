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
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PantryActivity extends AppCompatActivity {
    Button recipeButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        // Initializing database
        db = FirebaseFirestore.getInstance();

        // Get's single document from collection
//        DocumentReference docRef = db.collection("users").document("Testing");
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d("PantryActivity", "DocumentSnapshot data: " + document.getData());
//                    } else {
//                        Log.d("PantryActivity", "No such document");
//                    }
//                } else {
//                    Log.d("PantryActivity", "get failed with ", task.getException());
//                }
//            }
//        });

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
        final TableLayout stk = findViewById(R.id.displayLayout);
        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(" Item ID ");
        tv0.setTextColor(Color.WHITE);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText(" Item Name ");
        tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(" Edit Item ");
        tv2.setTextColor(Color.WHITE);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText(" Delete Item ");
        tv3.setTextColor(Color.WHITE);
        tbrow0.addView(tv3);
        stk.addView(tbrow0);
        // Gets all db entries
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final QueryDocumentSnapshot doc = document;
                                Log.d("PantryActivity", document.getId() + " => " + document.getData());
                                TableRow tbrow = new TableRow(PantryActivity.this);
                                TextView t1v = new TextView(PantryActivity.this);
                                t1v.setText(document.getId());
                                t1v.setTextColor(Color.BLACK);
                                t1v.setGravity(Gravity.CENTER);
                                tbrow.addView(t1v);
                                TextView t2v = new TextView(PantryActivity.this);
                                t2v.setText(document.getString("name"));
                                t2v.setTextColor(Color.BLACK);
                                t2v.setGravity(Gravity.CENTER);
                                tbrow.addView(t2v);
                                ImageButton editBtn = new ImageButton(PantryActivity.this);
                                editBtn.setImageResource(R.drawable.edit);
                                editBtn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        System.out.println("Hi");
                                    }
                                });
                                tbrow.addView(editBtn);
                                ImageButton deleteBtn = new ImageButton(PantryActivity.this);
                                deleteBtn.setImageResource(R.drawable.delete);
                                deleteBtn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        System.out.println("Hi");
                                        db.collection("users").document(doc.getId()).delete();
                                    }
                                });
                                tbrow.addView(deleteBtn);
                                stk.addView(tbrow);
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
        String[] ingredients = {"pork", "peppers", "rice", "onions"};
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

    private void invoke_recipe_pipeline() {
        String search_url = constructSearchUrl(getIngredients());
        get_recipe_and_load_page(search_url);
    }
}