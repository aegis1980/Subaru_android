package com.fitc.com.subaru;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void setHardwareButton(View v){
        Button b = (Button)v;
        Intent i = new Intent(this,AppSelectionActivity.class);
        startActivityForResult(i,b.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                ResolveInfo ri = data.getParcelableExtra(AppSelectionActivity.EXTRA_APP_INFO);
                SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String json = ri == null ? null : new Gson().toJson(ri);

                // Check which request we're responding to
                switch (requestCode){
                    case (R.id.info_button):
                        editor.putString(Constants.INFO_BUTTON, json).apply();
                        break;
                    case (R.id.menu_button):
                        editor.putString(Constants.MENU_BUTTON, json).apply();
                        break;
                    case (R.id.map_button):
                        editor.putString(Constants.MAP_BUTTON, json).apply();
                        break;
                    case (R.id.av_button):
                        editor.putString(Constants.AV_BUTTON, json).apply();
                        break;
                    case (R.id.media_button):
                        editor.putString(Constants.MEDIA_BUTTON, json).apply();
                        break;

                    //tell service the pref have changed.

                }
            }
        }
    }

