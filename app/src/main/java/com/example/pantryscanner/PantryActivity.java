package com.example.pantryscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

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