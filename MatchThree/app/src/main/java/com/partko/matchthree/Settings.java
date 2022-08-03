package com.partko.matchthree;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "myLogs";

    TextView description;
    Button btnBack;
    Button btnResetProgress;

    ProgressBar pgrsBar;

    DBHelper dbHelper = new DBHelper(this);
    int allLevels;
    int completedLevels;

    //private DBHelper mDBHelper;

    Switch themeSwitch;
    SeekBar seekSounds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Settings Activity");

        setContentView(R.layout.activity_settings);

        description = findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        themeSwitch = findViewById(R.id.switch_theme);
        seekSounds = findViewById(R.id.seekBarSounds);
        seekSounds.setOnSeekBarChangeListener(this);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        btnResetProgress = (Button) findViewById(R.id.btnResetProgress);
        btnResetProgress.setOnClickListener(this);

        themeSwitch.setChecked(MainActivity.sharedPrefs.getBoolean("isNightTheme", false));
        themeSwitch.setOnCheckedChangeListener(this);
        setNightTheme();

        seekSounds.setProgress(MainActivity.sharedPrefs.getInt("volume", 100));

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("arcade_results", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            //int level = cursor.getColumnIndex("level");
            int star = cursor.getColumnIndex("star");
            do {
                allLevels++;
                if (cursor.getInt(star) > 0) {
                    completedLevels++;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d(TAG, "allLevels " + allLevels);
        Log.d(TAG, "completedLevels " + completedLevels);


        //ConstraintLayout constraintLayout = findViewById(R.id.settings);
        //ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams
        //        (ConstraintLayout.LayoutParams.MATCH_PARENT / 2, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        pgrsBar = findViewById(R.id.progressBar);
        //pgrsBar.setLayoutParams(new ConstraintLayout.LayoutParams(1000, 200));
        //pgrsBar.setLayoutParams(layoutParams);

        pgrsBar.setMax(allLevels);
        pgrsBar.setProgress(completedLevels);

        //mDBHelper = new DBHelper(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnResetProgress:
                DBHelper.copyDatabase(this);
                pgrsBar.setProgress(0);
                break;
            case R.id.btnBack:
                //Intent intent = new Intent(this, MainActivity.class);
                //startActivity(intent);
                finish();
                break;
            default:
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Toast.makeText(this, "Отслеживание переключения: " + (isChecked ? "on" : "off"), Toast.LENGTH_SHORT).show();
        if (isChecked){
            SharedPreferences.Editor ed = MainActivity.sharedPrefs.edit();
            ed.putBoolean("isNightTheme", true);
            ed.apply();
            setNightTheme();
        } else {
            SharedPreferences.Editor ed = MainActivity.sharedPrefs.edit();
            ed.putBoolean("isNightTheme", false);
            ed.apply();
            setNightTheme();
        }
    }

    public void setNightTheme() {
        if (MainActivity.sharedPrefs.getBoolean("isNightTheme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SharedPreferences.Editor ed = MainActivity.sharedPrefs.edit();
        ed.putInt("volume", seekSounds.getProgress());
        ed.apply();
    }
}
