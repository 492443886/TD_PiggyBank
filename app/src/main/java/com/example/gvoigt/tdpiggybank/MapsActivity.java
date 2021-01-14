package com.example.gvoigt.tdpiggybank;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private GoogleApiClient googleApiClient;

    String id = "";
    String key = "";
    List<Pig> pigs = new ArrayList<Pig>();
    Pig pigForDrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        id = getIntent().getExtras().getString("id");

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReferenceFromUrl("https://fir-td-2f0d8.firebaseio.com/");


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // set up the googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this, this, this)
                .addApi(LocationServices.API).build();

    }


    ValueEventListener listener;
    private void findPig() {
        listener = databaseReference.child("Pig").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Pig pigt = new Pig();
                    pigt = ds.getValue(Pig.class);
                    if(pigt.isDroped() == true){
                        pigs.add(pigt);
                    }
                    if(pigt.getOwner().equals(id) && pigt.isDroped()== false){
                        key = ds.getKey();
                        pigForDrop = pigt;
                    }
                }
                findPigCallBack();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void findPigCallBack() {

        for(Pig p : pigs){

            LatLng loc = new LatLng(p.getLat(), p.getLon());

            String messgae = p.getOwner() + "' pig: $" + p.getAmount();


            mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(messgae));

        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();

            LatLng loc = new LatLng(lat, lon);
            // Add a BLUE marker to current location and zoom
            mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("The pig you drop").snippet("The pig you drop"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));


            databaseReference.child("Pig").removeEventListener(listener);
            DatabaseReference pigRef = databaseReference.child("Pig").child(key);

            pigRef.child("lat").setValue(lat);
            pigRef.child("lon").setValue(lon);

            pigRef.child("droped").setValue(true);


        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        findPig();
    }

    @Override
    public void onConnectionSuspended(int i) {



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();}
    }
    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();

    }
}
