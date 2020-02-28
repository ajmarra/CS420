package com.example.pantryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;


public class AddItemActivity extends AppCompatActivity {
    Button btn, openBtn, cameraBtn, addButton;
    TextView txtView;
    ImageView myImageView;
    Frame frame;
    BarcodeDetector detector;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        btn = findViewById(R.id.button);
        openBtn = findViewById(R.id.uploadButton);
        cameraBtn = findViewById(R.id.cameraButton);
        addButton = findViewById(R.id.addButton);
        interpret_upc("012000286209");
        interpret_upc("079200731427");
        // Initializing database
        db = FirebaseFirestore.getInstance();

        myImageView = findViewById(R.id.imgview);
        /*Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.puppy);
        myImageView.setImageBitmap(myBitmap);*/

        txtView = findViewById(R.id.txtContent);

        // Creates the barcode detector
        detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            txtView.setText("Could not set up the detector!");
            return;
        }

        /*frame = new Frame.Builder().setBitmap(myBitmap).build();
        final SparseArray<Barcode> barcodes = detector.detect(frame);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Barcode thisCode = barcodes.valueAt(0);
                txtView.setText(thisCode.rawValue);
            }
        }); */

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToCamera();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new user with a first and last name
                Map<String, Object> user = new HashMap<>();
                user.put("first", "Ada");
                user.put("last", "Lovelace");
                user.put("born", 1815);

                // Add a new document with a generated ID
                db.collection("users")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("MainActivity", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("MainActivity", "Error adding document", e);
                            }
                        });
            }
        });

    }

    private void uploadImage() {
        // Checks to see if it has permission to access photos.  If no then it asks for it.
        try {
            if (ActivityCompat.checkSelfPermission(AddItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddItemActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent choosePhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choosePhoto, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void switchToCamera() {
        // Probably should have some kind of permissions check here to access the camera?
        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent choosePhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(choosePhoto, 1);
                } else {
                    System.out.println("No gallery permission granted");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0: // Camera
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        myImageView.setImageBitmap(selectedImage);

                        // Barcode button can only be used if there is a photo present to analyze
                        frame = new Frame.Builder().setBitmap(selectedImage).build();
                        final SparseArray<Barcode> barcodes = detector.detect(frame);

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Barcode thisCode = barcodes.valueAt(0);
                                txtView.setText(thisCode.rawValue);
                            }
                        });
                    }

                    break;
                case 1: // Upload photo
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
                                myImageView.setImageBitmap(myBitmap);
                                cursor.close();

                                // Barcode button can only be used if there is a photo present to analyze
                                frame = new Frame.Builder().setBitmap(myBitmap).build();
                                final SparseArray<Barcode> barcodes = detector.detect(frame);

                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Barcode thisCode = barcodes.valueAt(0);
                                        txtView.setText(thisCode.rawValue);
                                    }
                                });
                            }
                        }
                    }
                    break;
            }
        }
    }


//
//    private String choose_recipe(List<String> all_recipes, String mode) {
//        String chosen_recipe_url;
//        if (mode.equals("random")) {
//            Random rand = new Random();
//            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
//        } else {
//            System.out.println("Only random url selection currently implemented. Defaulting to random.");
//            Random rand = new Random();
//            chosen_recipe_url = all_recipes.get(rand.nextInt(all_recipes.size()));
//        }
//        return chosen_recipe_url;
//    }
//
//    private boolean is_recipe_url(String possible_recipe) {
//        String url_template = "https://www.allrecipes.com/recipe/";
//        return url_template.regionMatches(0, possible_recipe, 0, 34);
//    }
//
//    private List<String> parse_webpage(Document document) {
//        Elements links = document.select("a[href]");
//        List<String> recipe_urls = new ArrayList<String>();
//        for (Element link : links) {
//            // get the value from the href attribute
//            if (is_recipe_url(link.attr("href"))) {
//                System.out.println("link: " + link.attr("href"));
//                recipe_urls.add(link.attr("href"));
//            }
//        }
//        return recipe_urls;
//    }
//
//    public interface Callable {
//        public void call(String param);
//    }
//
//    class BarcodeScraper implements Callable {
//        public void call(String scrape_url) {
//            System.out.println( param );
//        }
//    }
//
//
    private String parse_webpage(Document document) {
        Elements b_sections = document.select("p > b");

        List<String> possible_product_names = new ArrayList<String>();
        for (Element b : b_sections) {
            possible_product_names.add(b.text());
        }
        return possible_product_names.get(0);
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
                String product_name = parse_webpage(document);
                System.out.println(product_name);
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

    private void interpret_upc(String upc) {
//        String upc_string = Integer.toString(upc);
        String upc_search_url = "https://www.upcitemdb.com/upc/" + upc;

        Executor executor = new Invoker();
        executor.execute(new Webscraper(upc_search_url));
    }
}
