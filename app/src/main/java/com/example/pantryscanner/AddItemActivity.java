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
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


public class AddItemActivity extends AppCompatActivity {
    Button btn, openBtn, cameraBtn;
    TextView txtView;
    ImageView myImageView;
    Frame frame;
    BarcodeDetector detector;
    EditText itemName, itemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        btn = findViewById(R.id.button);
        openBtn = findViewById(R.id.uploadButton);
        cameraBtn = findViewById(R.id.cameraButton);

        itemName = findViewById(R.id.itemText);
        itemType = findViewById(R.id.itemType);

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

}
