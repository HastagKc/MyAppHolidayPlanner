package com.example.holidayplannerapp.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.holidayplannerapp.Database.Database;
import com.example.holidayplannerapp.Model.ProductDataModel;
import com.example.holidayplannerapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UpdateProductActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment smf;
    FusedLocationProviderClient client;
    GoogleMap mgoogleMap;
    NetworkInfo networkInfo;
    ConnectivityManager manager;
    List<Address> address;
    ArrayList<ProductDataModel> pdm;
    Database db;
    Geocoder geocoder;
    TextView productaddcamera, productaddgallery;
    Button productupdatebtn;
    ImageView productaddimage, backicon;
    EditText productaddname, productadddes, productaddquantity, productaddprice;
    final int CAMERA_CODE = 100;
    final int GALLERY_CODE = 200;
    Double lat, lng;
    ProductDataModel productDataModel;
    Integer id, catid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_update);
        id = getIntent().getExtras().getInt("productid");
        catid = getIntent().getExtras().getInt("pcid");
        db = new Database(this);

        // Change notification color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPurple));
        }

        // Initialize views
        productupdatebtn = findViewById(R.id.productupdatebtnid);
        productaddname = findViewById(R.id.productupdatetitleid);
        productaddquantity = findViewById(R.id.productupdatequantityid);
        productadddes = findViewById(R.id.productuodatedesid);
        productaddprice = findViewById(R.id.productupdatepriceid);
        productaddimage = findViewById(R.id.productupdateimageid);
        productaddgallery = findViewById(R.id.productupdatefromgallery);
        productaddcamera = findViewById(R.id.productupdatefromcamera);
        backicon = findViewById(R.id.ubackimgf);

        // Add product image from gallery
        productaddgallery.setOnClickListener(v -> {
            Intent igallery = new Intent(Intent.ACTION_PICK);
            igallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(igallery, GALLERY_CODE);
        });

        // Add product image from camera
        productaddcamera.setOnClickListener(v -> {
            Intent icamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(icamera, CAMERA_CODE);
        });
        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        smf.getMapAsync(this);

        productupdatebtn.setOnClickListener(v -> {
            AddProductActivity adp = new AddProductActivity();
            productDataModel = new ProductDataModel();
            productDataModel.setProductimage(adp.convertImageViewToByteArray(productaddimage));
            productDataModel.setProductname(productaddname.getText().toString());
            productDataModel.setProductquantity(Integer.parseInt(productaddquantity.getText().toString()));
            productDataModel.setProductprice(Double.parseDouble(productaddprice.getText().toString()));
            productDataModel.setProductdescription(productadddes.getText().toString());
            productDataModel.setProductstatus(pdm.get(0).getProductstatus());
            productDataModel.setProductcategoryid(pdm.get(0).getProductcategoryid());
            productDataModel.setProductlat(lat);
            productDataModel.setProductlong(lng);

            int result = db.updateproduct(productDataModel, pdm.get(0).getProductid());
            if (result == -1) {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateProductActivity.this, ProductListActivity.class);
                intent.putExtra("pcid", pdm.get(0).getProductcategoryid());
                startActivity(intent);
            }
        });

        // Back to product list activity
        backicon.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateProductActivity.this, ProductListActivity.class);
            intent.putExtra("pcid", pdm.get(0).getProductcategoryid());
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                // Handle camera image selection
                Bitmap img = (Bitmap) (data.getExtras().get("data"));
                productaddimage.setImageBitmap(img);
            } else if (requestCode == GALLERY_CODE) {
                // Handle gallery image selection
                productaddimage.setImageURI(data.getData());
            }
        } else if (resultCode == RESULT_CANCELED) {
            // Handle the case when the user cancels image selection
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mgoogleMap = googleMap;
        pdm = db.productfetchdataformapload(id);

        if (pdm != null && !pdm.isEmpty()) {
            productaddname.setText(pdm.get(0).getProductname());
            productaddquantity.setText(String.valueOf(pdm.get(0).getProductquantity()));
            productaddprice.setText(String.valueOf(pdm.get(0).getProductprice()));
            productadddes.setText(pdm.get(0).getProductdescription());
            Bitmap imageDataInBitmap = BitmapFactory.decodeByteArray(pdm.get(0).getProductimage(), 0, pdm.get(0).getProductimage().length);
            productaddimage.setImageBitmap(imageDataInBitmap);

            checkConnection();
            if (networkInfo.isConnected() && networkInfo.isAvailable()) {
                lat = pdm.get(0).getProductlat();
                lng = pdm.get(0).getProductlong();
                try {
                    getitemlocation(lat, lng);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please Check Network", Toast.LENGTH_SHORT).show();
            }

            mgoogleMap.setOnMapClickListener(latLng -> {
                mgoogleMap.clear();

                checkConnection();
                if (networkInfo.isConnected() && networkInfo.isAvailable()) {
                    lat = latLng.latitude;
                    lng = latLng.longitude;
                    try {
                        getitemlocation(lat, lng);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please Check Network", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Product data not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Check internet connectivity
    public void checkConnection() {
        manager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();
    }

    public void getitemlocation(Double mlat, Double mlng) throws IOException {
        geocoder = new Geocoder(UpdateProductActivity.this, Locale.getDefault());
        if (mlat != 0) {
            address = geocoder.getFromLocation(mlat, mlng, 1);
            if (address != null && !address.isEmpty()) {
                String mAddress = address.get(0).getAddressLine(0);
                String city = address.get(0).getLocality();
                String state = address.get(0).getAdminArea();
                String Country = address.get(0).getCountryName();
                String postalCode = address.get(0).getPostalCode();
                String knownName = address.get(0).getFeatureName();

                String productAddress = pdm.get(0).getProductname() + " " + mAddress;

                if (mAddress != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(mlat, mlng);
                    markerOptions.position(latLng).title(productAddress);

                    mgoogleMap.addMarker(markerOptions).showInfoWindow();
                    mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UpdateProductActivity.this, ProductListActivity.class);
        intent.putExtra("pcid", pdm.get(0).getProductcategoryid());
        startActivity(intent);
    }
}
