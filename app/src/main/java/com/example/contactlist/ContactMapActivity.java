package com.example.contactlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

public class ContactMapActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener gpsListener;
    final int PERMISSION_REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_map);
        initGetLocationButton();
        initListButton();
        initMapButton();
        initSettingsButton();
    }
    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setEnabled(false);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(ContactMapActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void initMapButton() {
        ImageButton ibMap = findViewById(R.id.imageButtonMap);
        ibMap.setEnabled(false);
        ibMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(ContactMapActivity.this, ContactMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void initSettingsButton() {
        ImageButton ibSettings = findViewById(R.id.imageButtonSettings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(ContactMapActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initGetLocationButton(){
        Button locationButton = (Button) findViewById(R.id.buttonGetLocation);
        locationButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
//                EditText editAddress = (EditText) findViewById(R.id.editAddressMap);
//                EditText editCity = (EditText) findViewById(R.id.editCityMap);
//                EditText editState = (EditText) findViewById(R.id.editStateMap);
//                EditText editZipCode = (EditText) findViewById(R.id.editZipcodeMap);
//
//                String address = editAddress.getText().toString() + ", " +
//                                    editCity.getText().toString() + ", " +
//                                    editState.getText().toString() + ", " +
//                                    editZipCode.getText().toString();
//
//                List<Address> addresses = null;
//                Geocoder geo = new Geocoder(ContactMapActivity.this);
//
//                TextView txtLatitude = (TextView) findViewById(R.id.textLatitude);
//                TextView txtLongitude = (TextView) findViewById(R.id.textLongitude);
//
//                txtLatitude.setText(String.valueOf(addresses.get(0).getLatitude()));
//                txtLongitude.setText(String.valueOf(addresses.get(0).getLongitude()));

                try {
                    if (Build.VERSION.SDK_INT >= 23){
                        if (ContextCompat.checkSelfPermission(ContactMapActivity.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED){
                            if (ActivityCompat.shouldShowRequestPermissionRationale
                                    (ContactMapActivity.this,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                                Snackbar.make(findViewById(R.id.activity_contact_map),
                                        "MyContactList requires this permission to locate " +
                                        "your contacts", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("OK", new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                ActivityCompat.requestPermissions(
                                                        ContactMapActivity.this,
                                                        new String[]{
                                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_REQUEST_LOCATION);
                                            }
                                        })
                                        .show();
                            } else{
                                ActivityCompat.requestPermissions(ContactMapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_LOCATION);
                            }
                        } else{
                            startLocationUpdates();
                        }
                    } else{
                        startLocationUpdates();
                    }
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(), "Error requesting permission",
                            Toast.LENGTH_LONG).show();
                }
            } ///

        });

    }
    @Override
    public void onPause(){
        super.onPause();
        if (Build.VERSION.SDK_INT >= 23 &&                     // v ???
                ContextCompat.checkSelfPermission(getBaseContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            locationManager.removeUpdates(gpsListener);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void startLocationUpdates(){

        if (Build.VERSION.SDK_INT >= 23 &&                       //v??
        ContextCompat.checkSelfPermission(getBaseContext(),
        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(getBaseContext(),
        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try{
            locationManager = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
            gpsListener = new LocationListener(){
                public void onLocationChanged(Location location){
                    TextView txtLatitude = (TextView) findViewById(R.id.textLatitude);
                    TextView txtLongitude = (TextView) findViewById(R.id.textLongitude);
                    TextView txtAccuracy = (TextView) findViewById(R.id.textAccuracy);
                    txtLatitude.setText(String.valueOf(location.getLatitude()));
                    txtLongitude.setText(String.valueOf(location.getLongitude()));
                    txtAccuracy.setText(String.valueOf(location.getAccuracy()));
                }
                public void onStatusChanged(String provider, int status, Bundle extras){}
                public void onProviderEnabled(String provider){}
                public void onProviderDisabled(String provider){}
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
        }


        catch (Exception e){
            Toast.makeText(getBaseContext(), "Error, Location not Available", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(ContactMapActivity.this,
                            "MyContactList will not locate your contacts.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
