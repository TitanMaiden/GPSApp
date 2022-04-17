package com.example.gpsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    // Constants
    public static final int DEFAULT_CHECK_INTERVAL = 10000;
    private static final int PERMISSION_FINE_LOCATION = 95;

    // UI Elements
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_updates, tv_address;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationsupdates;
    Button btn_map;

    LocationRequest locationRequest;
    Location lastLocation;

    // API Client
    FusedLocationProviderClient fusedLocationProviderClient;

    // Callback that is called whenever location is requested/removed.
    LocationCallback locationCallBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting UI elements
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        btn_map = findViewById(R.id.btn_map);

        //Setting location request to update every 10 sec and be very accurate
        locationRequest = new com.google.android.gms.location.LocationRequest();
        locationRequest.setInterval(DEFAULT_CHECK_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Setting location callaback
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
                updateUI(lastLocation);
            }
        };

        // Setting listener when "updates" switch is toggled.
        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsupdates.isChecked()) {
                    btn_map.setEnabled(true);
                    startLocationUpdate();
                } else {
                    btn_map.setEnabled(false);
                    stopLocationUpdate();
                }
            }
        });

       btn_map.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               MapsActivity.setCurrentLocation(lastLocation);
               Intent intent = new Intent(MainActivity.this, MapsActivity.class);
               startActivity(intent);
           }
       });

        //Update GPS on start of the app
        updateGPS();
    }

    // Stop getting location updates if updates switch is off.
    private void stopLocationUpdate() {
        tv_updates.setText("Location is not tracking.");
        tv_altitude.setText("Location is not tracking.");
        tv_accuracy.setText("Location is not tracking.");
        tv_lon.setText("Location is not tracking.");
        tv_address.setText("Location is not tracking.");
        tv_lat.setText("Location is not tracking.");
        tv_speed.setText("Location is not tracking.");
        tv_updates.setText("Off");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    // start getting location updates if switch is on, but before that check for permissions.
    private void startLocationUpdate() {
        tv_updates.setText("On");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    //request code = 95
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this,"This program needs permissions to be granted.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        //check if you have permissions to get location, if so, then get last location and update ui
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastLocation = location;
                    updateUI(location);
                }
            });
        }
        else {
            // Request location permissions
            // BUILD 23(OS) or highger, build code for 23 is M
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    // Update UI by getting the latest location updates info
    private void updateUI(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf((location.getAltitude())));
        }
        else{
            tv_altitude.setText("Not avaliable on your phone.");
        }
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Not avaliable on your phone.");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try{
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addressList.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("Exception was thrown while getting address.");
        }
    }
}