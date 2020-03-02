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
import android.widget.Toast;

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
    Button btn, openBtn, cameraBtn, addButton, pantryButton;
    TextView txtView, ingredientTxt;
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

        // Initializing database
        db = FirebaseFirestore.getInstance();

        myImageView = findViewById(R.id.imgview);

        txtView = findViewById(R.id.txtContent);
        ingredientTxt = findViewById(R.id.editText2);

        // Creates the barcode detector
        detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            txtView.setText("Could not set up the detector!");
            return;
        }

        // Switch to PantryActivity
        pantryButton = findViewById(R.id.pantryAct);
        pantryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(AddItemActivity.this, PantryActivity.class);
                startActivity(i);
            }
        });

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
                if (ingredientTxt.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Specify An Item To Add", Toast.LENGTH_LONG).show();
                }
                else {
                    // Create a new pantry item for testing
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", ingredientTxt.getText().toString());
                    item.put("type", "unknown");
                    item.put("quantity", 1);

                    // Add a new document with a generated ID
                    db.collection("pantry")
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
                }
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

                        if (barcodes.size() > 0) {
                            Barcode thisCode = barcodes.valueAt(0);
                            interpret_upc(thisCode.rawValue);
                        }
                        else {
                            Toast.makeText(AddItemActivity.this,"No Barcode Detected",Toast.LENGTH_SHORT).show();
                        }

//                        btn.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                if (barcodes.size() > 0) {
//                                    Barcode thisCode = barcodes.valueAt(0);
//                                    txtView.setText(thisCode.rawValue);
//                                }
//                                else {
//                                    Toast.makeText(AddItemActivity.this,"No Barcode Detected",Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
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

                                if (barcodes.size() > 0) {
                                    Barcode thisCode = barcodes.valueAt(0);
                                    interpret_upc(thisCode.rawValue);
                                }
                                else {
                                    Toast.makeText(AddItemActivity.this,"No Barcode Detected",Toast.LENGTH_SHORT).show();
                                }

//                                btn.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        if (barcodes.size() > 0) {
//                                            Barcode thisCode = barcodes.valueAt(0);
//                                            txtView.setText(thisCode.rawValue);
//                                        }
//                                        else {
//                                            Toast.makeText(AddItemActivity.this,"No Barcode Detected",Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
                            }
                        }
                    }
                    break;
            }
        }
    }

    private List<String> parse_upc_webpage(Document document) {
        Elements b_sections = document.select("p > b");
        List<String> possible_product_names = new ArrayList<String>();
        for (Element b : b_sections) {
            possible_product_names.add(b.text());
        }
        return possible_product_names;
    }

    public class BarcodeCallable implements Callable<Void> {
        private String search_url;

        public BarcodeCallable(String search_url) {
            this.search_url = search_url;
        }

        @Override
        public Void call() {
            try {
                Document document = Jsoup.connect(this.search_url).get();
                List<String> possible_product_names = parse_upc_webpage(document);
                if (possible_product_names.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No Product Found For Pictured Barcode", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    ingredientTxt.setText(possible_product_names.get(0));
                    ingredientTxt.invalidate();
                    ingredientTxt.requestLayout();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void interpret_upc(String upc) {
        String upc_search_url = "https://www.upcitemdb.com/upc/" + upc;
        Executor executor = new Invoker();
        Callable barcode_scrape_call = new BarcodeCallable(upc_search_url);
        executor.execute(new Webscraper(barcode_scrape_call));
    }
}
