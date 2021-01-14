package com.example.gvoigt.tdpiggybank;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //data
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<User> users = new ArrayList<User>();

    private EditText username;
    private EditText password;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);

        disableButtonSignIn();

        editTextUsername.addTextChangedListener(new EditTextListener());
        editTextPassword.addTextChangedListener(new EditTextListener());


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReferenceFromUrl("https://fir-td-2f0d8.firebaseio.com/");

        databaseReference.child("User").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren() ){
                    User user = new User();
                    user.setId(ds.getValue(User.class).getId());
                    user.setPassword(ds.getValue(User.class).getPassword());
                    users.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickSignIn(View view) {


        String idTyped = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        String passwordTyped = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();

        boolean isAuthenticated = false;
        for(User user : users){
            String id = user.getId();
            String password = user.getPassword();

            if(id.equals(idTyped) && password.equals( passwordTyped )){
                isAuthenticated = true;
            }

        }
        if (isAuthenticated) {
            //Intent intent = new Intent(this, DashboardActivity.class);
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("id", idTyped);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), "Your credentials are wrong. Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    private class EditTextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(editTextUsername.getText().toString().length() != 0 && editTextPassword.getText().toString().length() != 0)
                enableButtonSignIn();
            else
                disableButtonSignIn();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void disableButtonSignIn() {
        buttonSignIn.setEnabled(false);
        buttonSignIn.setBackgroundColor(getResources().getColor(R.color.colorGray));
        buttonSignIn.setTextColor(getResources().getColor(R.color.colorDarkGray));
    }

    private void enableButtonSignIn() {
        buttonSignIn.setEnabled(true);
        buttonSignIn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttonSignIn.setTextColor(getResources().getColor(R.color.colorWhite));
    }
}
