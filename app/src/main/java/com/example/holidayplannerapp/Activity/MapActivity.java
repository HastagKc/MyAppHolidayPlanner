package com.example.holidayplannerapp.Activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.holidayplannerapp.Activity.ProductListActivity;
import com.example.holidayplannerapp.Database.Database;
import com.example.holidayplannerapp.Model.ProductDataModel;
import com.example.holidayplannerapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment smf;
    FusedLocationProviderClient client;
    GoogleMap mgoogleMap;
    Geocoder geocoder;
    Double productlat, productlng;
    NetworkInfo networkInfo;
    ConnectivityManager manager;
    List<Address> address;
    String newitem = "";
    Database db;
    int procatid;
    Spinner spinner;
    ArrayList<ProductDataModel> productDataModels;
    ImageView backimg;
    TextView productname;
    String product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Change notification color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.colorPurple));
        }

        productname = findViewById(R.id.showselectedproduct);

        procatid = getIntent().getIntExtra("productid", -1);
        product = getIntent().getStringExtra("productname");

        if (product != null) {
            productname.setText(product);
        }

        smf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);
        db = new Database(this);

        if (smf != null) {
            smf.getMapAsync(this);
        }

        spinner = findViewById(R.id.productformapview);
        loadSpinnerData();

        // Going back to the product list activity
        backimg = findViewById(R.id.mbackimgf);
        backimg.setOnClickListener(view -> navigateToProductListActivity());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mgoogleMap = googleMap;

        // Check network connectivity
        checkConnection();

        // Handle map initialization errors
        if (mgoogleMap == null) {
            showError("Map is not ready yet");
            return;
        }

    }

    public void checkConnection() {
        manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();
    }

    private void GetAddress(double mlat, double mlng) {
        geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(mlat, mlng, 1);
            if (address != null && !address.isEmpty()) {
                String mAddress = address.get(0).getAddressLine(0);
                String productaddress = newitem + " " + mAddress;

                if (mgoogleMap != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng lating = new LatLng(mlat, mlng);
                    markerOptions.position(lating).title(productaddress);
                    mgoogleMap.addMarker(markerOptions).showInfoWindow();
                    mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lating, 17));

                    procatid = -1;
                } else {
                    showError("Map is not ready yet");
                }
            } else {
                showError("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("An error occurred: " + e.getMessage());
        }
    }

    private void loadSpinnerData() {
        Database db = new Database(getApplicationContext());
        ArrayList<ProductDataModel> lables = db.productfetchdataformap();
        String[] nameList = new String[lables.size()];

        for (int i = 0; i < lables.size(); i++) {
            nameList[i] = lables.get(i).getProductname();
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nameList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mgoogleMap.clear();
                if (procatid < 0) {
                    newitem = spinner.getSelectedItem().toString();
                    productlat = lables.get(position).getProductlat();
                    productlng = lables.get(position).getProductlong();
                } else {
                    productDataModels = db.productfetchdataformapload(procatid);
                    if (!productDataModels.isEmpty()) {
                        productlat = productDataModels.get(0).getProductlat();
                        productlng = productDataModels.get(0).getProductlong();
                    } else {
                        showError("No product data found.");
                    }
                }
                if (productlng != 0.0) {
                    GetAddress(productlat, productlng);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void navigateToProductListActivity() {
        Intent intent = new Intent(MapActivity.this, ProductListActivity.class);
        startActivity(intent);
        finish();
    }

    // Handle errors by displaying a toast message
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        try {
            navigateToProductListActivity();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("An error occurred: " + ex.getMessage());
        }
    }
}
