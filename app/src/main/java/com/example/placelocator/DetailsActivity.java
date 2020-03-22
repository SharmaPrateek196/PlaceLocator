package com.example.placelocator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private  PlacePojo placePojo;
    private TextView name,desc,rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        placePojo=(PlacePojo) getIntent().getSerializableExtra("PlaceObj");

        name=(TextView)findViewById(R.id.tv_name_ad);
        desc=(TextView)findViewById(R.id.tv_desc_ad);
        rating=(TextView)findViewById(R.id.tv_rating_ad);

        name.setText(placePojo.getName());
        desc.setText(placePojo.getTypes());
        rating.setText(String.valueOf(placePojo.getRating()));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng dest = new LatLng(placePojo.getLatitude(), placePojo.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(dest)
                .title(placePojo.getName()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
    }
}
