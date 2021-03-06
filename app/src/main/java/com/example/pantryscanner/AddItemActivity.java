package com.example.pantryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import io.opencensus.internal.Utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static java.security.AccessController.getContext;


public class AddItemActivity extends AppCompatActivity {
    Button openBtn, cameraBtn, addButton, pantryButton;
    TextView instructTxt, ingredientTxt;
    ImageView myImageView;
    String userId, currentPhotoPath;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        // Gets the userID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        openBtn = findViewById(R.id.uploadButton);
        cameraBtn = findViewById(R.id.cameraButton);
        addButton = findViewById(R.id.addButton);

        // Initializing database
        db = FirebaseFirestore.getInstance();

        myImageView = findViewById(R.id.imgview);

        instructTxt = findViewById(R.id.textView2);
        ingredientTxt = findViewById(R.id.editText2);

        // Creates the barcode detector
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                        .build();


        // Switch to PantryActivity
        pantryButton = findViewById(R.id.pantryAct);
        pantryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(AddItemActivity.this, PantryActivity.class);
                startActivity(i);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ingredientTxt.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Specify An Item To Add", Toast.LENGTH_LONG).show();
                }
                else {
                    // Create a new pantry item document
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", ingredientTxt.getText().toString());
                    item.put("type", "unknown");
                    item.put("quantity", 1);

                    // Add a new document with a generated ID
                    db.collection(userId)
                            .add(item)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("AddItemActivity", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(AddItemActivity.this, "Item Added!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("AddItemActivity", "Error adding document", e);
                                }
                            });
                    ingredientTxt.setText("");
                }
            }
        });

        instructTxt.setText("Scan a barcode or enter your product manually!");



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
//        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(takePicture, 0);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pantryscanner.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 0);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file://" + image.getAbsolutePath();
        return image;
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
                    Toast.makeText(AddItemActivity.this,"No gallery permission granted",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AddItemActivity.this, Integer.toString(resultCode) ,Toast.LENGTH_SHORT).show();
                        try {
                            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(currentPhotoPath));
                            myImageView.setImageBitmap(selectedImage);
                            // Sets image to scan
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(selectedImage);
                            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                                    .getVisionBarcodeDetector();
                            // If barcode detected then it webscrapes the upc site
                            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                            if (barcodes.size() > 0) {
                                                FirebaseVisionBarcode thisCode = barcodes.get(0);
                                                interpret_upc(thisCode.getRawValue());
                                            }
                                            else {
                                                Toast.makeText(AddItemActivity.this,"No barcode detected",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AddItemActivity.this,"Failed to load image",Toast.LENGTH_SHORT).show();
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

                                // Sets the image to scan
                                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
                                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                                        .getVisionBarcodeDetector();
                                // If barcode detected then it webscrapes the upc site
                                Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                                if (barcodes.size() > 0) {
                                                    FirebaseVisionBarcode thisCode = barcodes.get(0);
                                                    interpret_upc(thisCode.getRawValue());
                                                }
                                                else {
                                                    Toast.makeText(AddItemActivity.this,"No Value In Barcode",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddItemActivity.this,"No Barcode Detected",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                    break;
            }
        }
    }

    // Grabs product names from the upc interpreting website
    private List<String> parse_upc_webpage(Document document) {
        Elements b_sections = document.select("p > b");
        List<String> possible_product_names = new ArrayList<String>();
        for (Element b : b_sections) {
            possible_product_names.add(b.text());
        }
        return possible_product_names;
    }

    //  A Callable class to pass into an asynchronous task with specific webscraping instructions
    public class BarcodeCallable implements Callable<Void> {
        private String search_url;
        private List<String> possible_product_names;

        public BarcodeCallable(String search_url) {
            this.search_url = search_url;
        }

        @Override
        public Void call() {
            try {
                Document document = Jsoup.connect(this.search_url).get();
                possible_product_names = parse_upc_webpage(document);
                if (possible_product_names.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No Product Found For Pictured Barcode", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ingredientTxt.setText(possible_product_names.get(0));
                        }
                    });

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Builds class objects and executes methods to set off upc analysis, adds found product to textView
    private void interpret_upc(String upc) {
        String upc_search_url = "https://www.upcitemdb.com/upc/" + upc;
        Executor executor = new Invoker();
        Callable barcode_scrape_call = new BarcodeCallable(upc_search_url);
        executor.execute(new Webscraper(barcode_scrape_call));
    }
}
