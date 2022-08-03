package com.partko.matchthree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "myLogs";

    Button btnArcade;
    Button btnTime;
    Button btnAbout;

    private DBHelper mDBHelper;

    public static SharedPreferences sharedPrefs;
    ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity Activity");

        setContentView(R.layout.activity_main);
        img = findViewById(R.id.imageView);

        sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if (!sharedPrefs.contains("initialized")) {
            ed = sharedPrefs.edit();

            // Флаг инициализации
            ed.putBoolean("initialized", true);

            // Установка значений по умолчанию
            ed.putBoolean("isNightTheme", false);
            ed.putInt("volume", 50);
            ed.putInt("timeRecord", 0);
            ed.apply();
        }

        setNightTheme();

        if (sharedPrefs.getBoolean("isNightTheme", false)) {
            img.setImageResource(R.drawable.background_black);
        } else {
            img.setImageResource(R.drawable.background);
        }

        btnArcade = (Button) findViewById(R.id.button_arcade);
        btnArcade.setOnClickListener(this);
        btnTime = (Button) findViewById(R.id.button_time);
        btnTime.setOnClickListener(this);
        btnAbout = (Button) findViewById(R.id.button_about);
        btnAbout.setOnClickListener(this);

        mDBHelper = new DBHelper(this);

        // Проверка на существование БД в памяти устройства
        File database = getApplicationContext().getDatabasePath(DBHelper.DBNAME);
        Log.d(TAG, "DB " + database);
        if (!database.exists()) {
            mDBHelper.getReadableDatabase();

            if (DBHelper.copyDatabase(this)) { // Копирование БД
                Log.d(TAG, "БД успешно скопирована в память устройства");
            } else {
                Log.d(TAG, "не получилось скопировать БД в память устройства");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_arcade:
                Intent intent = new Intent(this, ArcadeLevels.class);
                startActivity(intent);
                break;

            case R.id.button_time:
                Intent intent2 = new Intent(this, TimeGame.class);
                startActivity(intent2);
                break;

            case R.id.button_about:
                Intent intent3 = new Intent(this, Settings.class);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }

    public void setNightTheme() {
        if (sharedPrefs.getBoolean("isNightTheme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}

