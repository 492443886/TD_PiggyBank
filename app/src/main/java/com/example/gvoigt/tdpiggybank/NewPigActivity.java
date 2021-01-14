package com.example.gvoigt.tdpiggybank;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewPigActivity extends AppCompatActivity {


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    Calendar myCalendar;
    EditText editTextDate;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pig);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReferenceFromUrl("https://fir-td-2f0d8.firebaseio.com/");

        id = getIntent().getExtras().getString("id");

        myCalendar = Calendar.getInstance();

        editTextDate = findViewById(R.id.editTextDate);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        editTextDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(NewPigActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editTextDate.setText(sdf.format(myCalendar.getTime()));

        closeKeyboard();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public void onCreate(View view) {

        String date = ((EditText)findViewById(R.id.editTextDate)).getText().toString();
        String anomont = ((Spinner)findViewById(R.id.spinner)).getSelectedItem().toString();
        boolean IsPrivate = ((Switch)findViewById(R.id.switch1)).isChecked();

        Pig pig = new Pig();

        pig.setOwner(id);
        pig.setDroped(false);
        pig.setDate(date);
        pig.setGoal(Double.parseDouble(anomont));
        pig.setPrivate(IsPrivate);

        DatabaseReference pigRef = databaseReference.child("Pig").push();
        pigRef.setValue(pig);


        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);


    }
}
