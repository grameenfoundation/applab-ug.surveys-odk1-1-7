/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import applab.client.ApplabActivity;
import applab.client.dataconnection.DataConnectionManager;
import applab.client.location.GpsManager;
import applab.client.surveys.R;
import applab.surveys.client.GlobalConstants;

/**
 * Responsible for displaying buttons to launch the major activities. Launches some activities based
 * on returns of others.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends ApplabActivity {
    private static final String t = "MainMenuActivity";

    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;
    private Button mGetFormsButton;
    private Button registerFarmerButton;
    
    private AlertDialog mAlertDialog;

    private static boolean EXIT = true;
    private EditText farmerNameEditBox;

    // private static boolean DO_NOT_EXIT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an external intent
        Log.i(t, "Starting up, creating directories");
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        setContentView(R.layout.main_menu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        this.farmerNameEditBox = (EditText)findViewById(R.id.id_field);
        
        
        
        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setText(getString(R.string.enter_data_button));
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start GPS search as a survey is being filled.
                GpsManager.getInstance().update();
                String farmerName = farmerNameEditBox.getText().toString().trim();
                
                if (farmerName.length() > 0) {
                    if (checkID(farmerName)) {
                        GlobalConstants.intervieweeName = farmerName;
                        tryOpenFormChooser();
                    }
                    else {
                        showTestSurveyDialog();
                    }
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.empty_respondent_id), Toast.LENGTH_SHORT);
                    toast.show();
                }
                
                /*Intent i = new Intent(getApplicationContext(), FormChooserList.class);
                startActivity(i);*/
            }
        });
        registerFarmerButton = (Button) findViewById(R.id.register_farmer_button);
        registerFarmerButton.setText(getString(applab.client.R.string.register_new_farmer));
        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setText(getString(R.string.review_data_button));
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                startActivity(i);
            }
        });

        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setText(getString(R.string.send_data_button));
        mSendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceUploaderList.class);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        mGetFormsButton = (Button) findViewById(R.id.get_forms);
        mGetFormsButton.setText(getString(R.string.get_forms));
        mGetFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormDownloadList.class);
                startActivity(i);

            }
        });

        // manage forms button. no result expected.
        mManageFilesButton = (Button) findViewById(R.id.manage_forms);
        mManageFilesButton.setText(getString(R.string.manage_files));
        mManageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FileManagerTabs.class);
                startActivity(i);
            }
        });
    }


    protected void tryOpenFormChooser() {
    	 GpsManager.getInstance().update();
    	 Intent i = new Intent(getApplicationContext(), FormChooserList.class);
         startActivity(i);
	}


	@Override
    protected void onPause() {
        super.onPause();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.general_preferences)).setIcon(
            android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent ig = new Intent(this, PreferencesActivity.class);
                startActivity(ig);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    protected void onStart() {
        super.onStart();

        // We need to display only one settings screen at a time.
        // So if no settings screen shown for GPS, try show that of mobile data, if disabled.
        // Every time a settings screen is closed, Activity:onStart() will be called and hence
        // help us ensure that we display all the settings screen we need, but one a time.
        if (!GpsManager.getInstance().onStart(this)) {
            DataConnectionManager.getInstance().onStart(this);
        }
    }
    
    private boolean checkID(String text) {
        Pattern pattern = Pattern.compile("[a-zA-Z]{2}[0-9]{4,5}+");
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
    
    void showTestSurveyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.help);
        builder.setTitle(getString(R.string.perform_test_survey));
        builder.setMessage(
                getString(R.string.test_survey_msg1)
                        + getString(R.string.test_survey_msg2)
                        + getString(R.string.test_survey_msg3)
                        + getString(R.string.test_survey_msg4))
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GlobalConstants.intervieweeName = "TEST";
                                tryOpenFormChooser();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
