package com.example.holidayplannerapp.Activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.holidayplannerapp.Database.Database;
import com.example.holidayplannerapp.Model.ProductDataModel;
import com.example.holidayplannerapp.R;

import java.util.ArrayList;
import java.util.List;

public class SmsActivity extends AppCompatActivity {

    EditText smobile, stitle, sdes, squantity, sprice, slocation;
    Button btnsendsms;
    ImageView backimg;
    Integer pid;
    Geocoder geocoder;
    List<Address> address;
    Database db = new Database(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sms);

        // Change notification color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPurple));
        }

        smobile = findViewById(R.id.smobile);
        stitle = findViewById(R.id.stitle);
        sdes = findViewById(R.id.sdescription);
        squantity = findViewById(R.id.squnatity);
        sprice = findViewById(R.id.sprice);
        btnsendsms = findViewById(R.id.btnsendsms);
        slocation = findViewById(R.id.slocation);
        backimg = findViewById(R.id.sbackimgf);

        pid = getIntent().getIntExtra("productid", -1);

        if (pid != -1) {
            settext();
        }

        // Call SendMessage method
        btnsendsms.setOnClickListener(v -> {
            sendMessage();
        });

        backimg.setOnClickListener(view -> {
            navigateToDashboardActivity();
        });
    }

    // SMS send method
    public void sendMessage() {
        String stringPhone = smobile.getText().toString().trim();
        String stringtitle = stitle.getText().toString().trim();
        String stringdes = sdes.getText().toString().trim();
        String stringquantity = squantity.getText().toString().trim();
        String stringprice = sprice.getText().toString().trim();
        String fullmessage = "Title: " + stringtitle + "\nDescription: " + stringdes + "\nQuantity: " + stringquantity +
                "\nPrice: " + stringprice;

        if (stringPhone.isEmpty() || stringtitle.isEmpty() || stringquantity.isEmpty() || stringprice.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(stringPhone, null, fullmessage, null, null);
                Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void settext() {
        ArrayList<ProductDataModel> pdm = db.productfetchdataformapload(pid);
        if (!pdm.isEmpty()) {
            stitle.setText(pdm.get(0).getProductname());
            sdes.setText(pdm.get(0).getProductdescription());
            squantity.setText(String.valueOf(pdm.get(0).getProductquantity()));
            sprice.setText(String.valueOf(pdm.get(0).getProductprice()));
            slocation.setText(pdm.get(0).getProductaddress());
        } else {
            Toast.makeText(this, "No product data found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        smobile.getText().clear();
        stitle.getText().clear();
        sdes.getText().clear();
        squantity.getText().clear();
        sprice.getText().clear();
    }

    private void navigateToDashboardActivity() {
        Intent intent = new Intent(SmsActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToDashboardActivity();
    }
}
