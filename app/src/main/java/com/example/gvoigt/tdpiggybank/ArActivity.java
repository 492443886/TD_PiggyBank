package com.example.gvoigt.tdpiggybank;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ArActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final double MIN_OPENGL_VERSION = 3.0;

    ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    List<Pig> pigs = new ArrayList<Pig>();
    List<String> keys = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReferenceFromUrl("https://fir-td-2f0d8.firebaseio.com/");




        // make sure OpenGL version is supported
        if (!checkIsSupportedDevice(this)) {
            String errorMessage =  "Sceneform requires OpenGL ES " + MIN_OPENGL_VERSION + " or later";
            Log.e(TAG, errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            finish(); // finish the activity
            return;
        }

        setContentView(R.layout.activity_ar);

        setupArScene();

    }

    protected void onStart(){
        super.onStart();
        findPig();
    }

    protected void onStop(){
        super.onStop();
        pigs.clear();
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
                        keys.add(ds.getKey());
                        pigs.add(pigt);
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

        databaseReference.child("Pig").removeEventListener(listener);

    }

    void setupArScene() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // load the renderabless
        //buildAndroidWidgetModel();
        build3dModel();

        // handle taps
        handleUserTaps();


    }

    private void handleUserTaps() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // viewRenderable must be loaded
            if (modelRenderable ==  null) {
                return;
            }

            //databaseReference.child("Pig").removeEventListener(listener);
            if(pigs.size()> 0){


                Pig p = pigs.get(0);
                pigs.remove(0);


                String msg = p.getOwner() + "' Pig found\n" +
                        "There are $" + p.getAmount() + "in it";
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();

                // create the an anchor on the scene
                AnchorNode anchorNode = createAnchorNode(hitResult);

                // add the view to the scene
                addRenderableToScene(anchorNode, modelRenderable);


                databaseReference.child("Pig").child(keys.get(0)).removeValue();
                keys.remove(0);

            }
            else{
                Toast.makeText(getApplicationContext(),"No pig found",Toast.LENGTH_SHORT).show();
            }

        });
    }

    private AnchorNode createAnchorNode(HitResult hitResult) {

        // create an anchor based off the the HitResult (what was tapped)
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);

        // attach this anchor to the scene
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        return anchorNode;
    }

    private Node addRenderableToScene(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        // anchor node knows where it fits into our world
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        node.select();

        return node;
    }

//    private void buildAndroidWidgetModel() {
//
//        ModelRenderable.builder()
//                // To load as an asset from the 'assets' folder ('src/main/assets/pig.sfb'):
//                .setSource(this, Uri.parse("pig.sfb"))
//
//                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
//                //.setSource(this, R.raw.andy)
//
//                .build()
//                .thenAccept(renderable -> modelRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Unable to load Renderable.", throwable);
//                            return null;
//                        });
//    }

    private void build3dModel() {

        ModelRenderable.builder()
                .setSource(this, Uri.parse("pig.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(ArActivity.this, "Unable to display model",
                            Toast.LENGTH_LONG).show();

                    return null;
                });
    }


    private boolean checkIsSupportedDevice(final Activity activity) {

        ActivityManager activityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Log.e(TAG, "ActivityManager is null");
            return false;
        }

        String openGlVersion = activityManager.getDeviceConfigurationInfo().getGlEsVersion();

        return openGlVersion != null && Double.parseDouble(openGlVersion) >= MIN_OPENGL_VERSION;
    }
}
