package com.example.placelocator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final int REQ_CODE =99 ;
    private GoogleMap mMap;
    private DatabaseHelperClass myHelper;
    private SQLiteDatabase dbWritableObj;
    private SQLiteDatabase dbReadableObj;

    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private EditText et_search;
    private Button btn_search;
    private RecyclerView recyclerView;

    private RadioButton rb_distance,rb_rating;

    private DestinationsListAdapter destinationsListAdapter;

    private double curLatitude,curLongitude;
    private  int destChecked=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        et_search=(EditText)findViewById(R.id.et_search);
        btn_search=(Button)findViewById(R.id.btn_search);
        rb_distance=(RadioButton)findViewById(R.id.rb_distance);
        rb_rating=(RadioButton)findViewById(R.id.rb_rating);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myHelper = new DatabaseHelperClass(MapsActivity.this);
        dbWritableObj = myHelper.getWritableDatabase();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searched_string="";
                searched_string=et_search.getText().toString();
                if(searched_string.equals(""))
                {
                    Toast.makeText(MapsActivity.this, "Please enter a destination.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(destChecked==1)
                        Toast.makeText(MapsActivity.this, "Sorted by distance(Ascending)", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MapsActivity.this, "Sorted by ratings(Descending)", Toast.LENGTH_SHORT).show();
                    View myview= getLayoutInflater().inflate(R.layout.destination_list_dialog,null);
                    AlertDialog.Builder builder=new AlertDialog.Builder(MapsActivity.this);
                    builder.setView(myview);
                    final AlertDialog alertDialog=builder.create();

                    if(myHelper!=null)
                    {
                        dbReadableObj=myHelper.getReadableDatabase();
                        Cursor cursor;
                            cursor=dbReadableObj.rawQuery("select * from place_temples "+
                                    "where name Like \""+searched_string.trim()+
                                    "%\" ",null);

                        ArrayList<PlacePojo> list=new ArrayList<>();
                        if(cursor.getCount()>0)
                        {
                            alertDialog.show();
                            int i=1;
                            while(i<=cursor.getCount())
                            {
                                cursor.moveToNext();
                                PlacePojo placePojo=new PlacePojo();
                                placePojo.setPlace_id(cursor.getString(0));
                                placePojo.setLatitude(cursor.getDouble(1));
                                placePojo.setLongitude(cursor.getDouble(2));
                                placePojo.setName(cursor.getString(3));
                                placePojo.setRating(cursor.getDouble(4));
                                placePojo.setTypes(cursor.getString(5));
                                placePojo=addDistanceInThisObj(placePojo);
                                list.add(placePojo);
                                i++;
                            }

                            if(rb_rating.isChecked())
                            {
                                destChecked=0;
                                Collections.sort(list,new SortByRating());
                            }
                            else
                            {
                                destChecked=1;
                                Collections.sort(list,new SortByDistance());
                            }

                            destinationsListAdapter=new DestinationsListAdapter(MapsActivity.this,list);
                            recyclerView=(RecyclerView)myview.findViewById(R.id.rv);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MapsActivity.this));
                            recyclerView.setAdapter(destinationsListAdapter);

                            destinationsListAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(MapsActivity.this, "No results found. Please search for a right place.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    private PlacePojo addDistanceInThisObj(PlacePojo placePojo) {
        //lat and long in radians
        double mylat=curLatitude/57.29577951;
        double mylong=curLongitude/57.29577951;
        double lat1=(placePojo.getLatitude())/57.29577951;
        double long1=(placePojo.getLongitude())/57.29577951;
        double dlon1 = long1 - mylong;
        double dlat1 = lat1 - mylat;
        double a = Math.pow(Math.sin(dlat1 / 2), 2)
                + Math.cos(mylat) * Math.cos(lat1)
                * Math.pow(Math.sin(dlon1 / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers.
        double r = 6371;

        double distance=r*c;
        placePojo.setDistance(distance);
        return placePojo;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQ_CODE);
            }
            else
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
            et_search.setClickable(false);
            btn_search.setClickable(false);
        } else {
            // already permission granted
            et_search.setClickable(true);
            btn_search.setClickable(true);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLastLocation = location;
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    //Place current location marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    curLatitude=location.getLatitude();
                    curLongitude=location.getLongitude();
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        et_search.setClickable(true);
                        btn_search.setClickable(true);
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
                    }
                    catch (SecurityException e)
                    {e.printStackTrace();}
                }
                else
                {
                    Toast.makeText(this, "Location Permission is not granted.\nReopen app and grant permission.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case REQ_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                    }
                    catch (SecurityException e)
                    {e.printStackTrace();}
                }
                else
                {
                    Toast.makeText(this, "Location Permission is not granted.\nPlease reopen the app and grant the permission.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            }
        }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
