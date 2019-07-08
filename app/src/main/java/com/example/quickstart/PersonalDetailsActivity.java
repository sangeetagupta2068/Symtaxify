package com.example.quickstart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;

public class PersonalDetailsActivity extends AppCompatActivity {
    //variable declaration
    private String yearOfJoiningValue;
    private String genderValue;
    private ArrayList<String> yearsOfJoiningList;

    //view declaration
    private RadioGroup genderRadioGroup;
    private Button submit;
    private EditText editTextRespondentNumber;
    private EditText editTextRespondentName;
    private Spinner yearOfJoiningItem;
    private RadioButton genderRadioGroupItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);
        initializePersonalDetailsSection();
        setListeners();
    }

    private void initializePersonalDetailsSection() {
        //variable initialization
        genderValue = "Male";

        //view initialization
        submit = findViewById(R.id.button2);
        editTextRespondentName = findViewById(R.id.respondent_name_edit_text);
        editTextRespondentNumber = findViewById(R.id.respondent_contact_number_edit_text);
        genderRadioGroup = findViewById(R.id.gender);
        yearsOfJoiningList = new ArrayList<>();
        yearOfJoiningItem = findViewById(R.id.spinner);

        //setting year of joining values to year of joining item
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2000; i <= thisYear; i++) {
            yearsOfJoiningList.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearsOfJoiningList);
        yearOfJoiningItem.setAdapter(adapter);

    }

    private void setListeners() {
        //setting listener to year of joining spinner item
        yearOfJoiningItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearOfJoiningValue = yearsOfJoiningList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                yearOfJoiningValue = yearsOfJoiningList.get(0);
            }
        });

        //setting listener to gender radio group
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderRadioGroupItem = findViewById(checkedId);
                genderValue = genderRadioGroupItem.getText().toString();
            }
        });

        //setting listener to submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logging values
                String value = "\nName: " + editTextRespondentName.getText().toString()
                        + "\nGender: " + genderValue
                        + "\nContact number: " + editTextRespondentNumber.getText().toString() + "\nYear of joining:" + yearOfJoiningValue;
                Log.i("PERSONAL_DETAILS_VALUES", value);

                //exiting personal details activity
                finish();
            }
        });
    }
}
