package com.example.quickstart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SectionListActivity extends AppCompatActivity {
    //view declaration
    private TextView personalDetailsSectionButton;
    private TextView viewAllRecordsSectionButton;
    private TextView logoutSectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_list);
        initializeSectionScreen();
        setListeners();
    }

    private void initializeSectionScreen() {
        //view initialization
        personalDetailsSectionButton = findViewById(R.id.personal_details_section);
        viewAllRecordsSectionButton = findViewById(R.id.view_all_records_section);
        logoutSectionButton = findViewById(R.id.logout_section);
    }

    private void setListeners() {
        //setting click attribute to personal details
        personalDetailsSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SectionListActivity.this, PersonalDetailsActivity.class);
                startActivity(intent);
            }
        });

        //setting click attribute to view all records
        viewAllRecordsSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SectionListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //setting click attribute to logout screen
        logoutSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
