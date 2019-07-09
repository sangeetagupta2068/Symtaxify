package com.example.quickstart;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.quickstart.MainActivity.REQUEST_ACCOUNT_PICKER;
import static com.example.quickstart.MainActivity.REQUEST_AUTHORIZATION;
import static com.example.quickstart.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.example.quickstart.MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

public class PersonalDetailsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    //variable declaration
    private String yearOfJoiningValue;
    private String genderValue;
    private ArrayList<String> yearsOfJoiningList;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    //change to write mode
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

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
        genderValue = "1";
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        //initializing credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

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
                if(genderValue.compareTo("Female") == 0){
                    genderValue = "2";
                }else{
                    genderValue = "1";
                }
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
                getResultsFromApi();

//                //exiting personal details activity
//                finish();
            }
        });
    }

    private void getResultsFromApi() {
        //check if google play services are available
        if (!isGooglePlayServicesAvailable()) {
            //if not then install
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            //choose user google account for accessing sheet
            chooseAccount();
        } else if (!isDeviceOnline()) {
            //if no network, display no network message
            Toast.makeText(this,"No network connection available.",Toast.LENGTH_SHORT).show();
        } else {
            //start background task for retrieving data from Google Sheets
            new PersonalDetailsActivity.MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            //if permission exists, get user account name in private mode
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                //set credential user name to account name if account name isn't null
                mCredential.setSelectedAccountName(accountName);
                //call this again to connect to API
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        //do nothing
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        //do nothing
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                PersonalDetailsActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = "1qlAiJVvYUTjSYkLHjnFRgGVW5m9U2zYGy0hNZFKz_d8";
//            String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            String range = "Sheet1!A2:D";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                results.add("\n\n");
                for (List row : values) {
                    results.add("Name: " + row.get(0) + " Gender: " + row.get(1) + " Contact Number:" + row.get(2) + " Year of Joining" + row.get(3) + "\n\n");
                }
            }
            return results;
        }


        @Override
        protected void onPreExecute() {
//            .setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(PersonalDetailsActivity.this,"No results returned.",Toast.LENGTH_SHORT).show();
            } else {
                output.add(0, "Data retrieved from Google Sheets:");
                Toast.makeText(PersonalDetailsActivity.this,TextUtils.join("\n", output),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    Log.e("ERROR_MESSAGE","The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.e("REQUEST_CANCELLED","Request cancelled.");
            }
        }
    }
}
