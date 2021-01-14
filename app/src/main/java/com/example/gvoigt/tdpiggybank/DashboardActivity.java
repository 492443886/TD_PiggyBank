package com.example.gvoigt.tdpiggybank;

import android.content.Intent;
import android.net.sip.SipSession;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    //data
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private User user;

    private TextView textViewName;

    Pig pig;
    String key = "";
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReferenceFromUrl("https://fir-td-2f0d8.firebaseio.com/");

        id = getIntent().getExtras().getString("id");

        textViewName = findViewById(R.id.textViewName);

        databaseReference.child("User").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren() ){
                    User userT = new User();
                    userT = ds.getValue(User.class);
                    if (userT.getId().equals(id)){
                        user = userT;
                    }


                }
                setup();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onResume()
    {
        super.onResume();
        findPig();

    }
    protected void onPause()
    {
        super.onPause();

        databaseReference.child("Pig").removeEventListener(listener);

        String id = "";
        String key = "";
        pig = null;

    }

    void setup(){
        textViewName.setText(user.getFirstName() + " " + user.getLastName() + "!");
    }


    public void onClickDrop(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }


    ValueEventListener listener;
    private void findPig() {


        listener = databaseReference.child("Pig").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Pig pigt = new Pig();
                    pigt = ds.getValue(Pig.class);
                    if (pigt.getOwner().equals(id) && pigt.isDroped() == false) {
                        key = ds.getKey();
                        pig = pigt;
                        break;
                    }
                }
                setupPig();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void setupPig(){
        if(pig == null){
            Intent intent = new Intent(this, NewPigActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);

        }else {
            TextView amountV = (TextView) findViewById(R.id.textView6);
            TextView dateV = (TextView) findViewById(R.id.textView9);
            TextView goalV = (TextView) findViewById(R.id.textView8);

            amountV.setText("$" + pig.getAmount());
            dateV.setText(pig.getDate());
            goalV.setText("Goal: $" + pig.getGoal() );
        }
    }


    public void onFeed(View view) {

        DatabaseReference pigRef = databaseReference.child("Pig").child(key).child("amount");

        pigRef.setValue(pig.getAmount() + pig.getGoal()* 0.05);

    }

    public void onClickFind(View view) {
        Intent intent = new Intent(this, ArActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
