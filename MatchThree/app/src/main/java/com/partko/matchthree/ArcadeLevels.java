package com.partko.matchthree;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.partko.matchthree.databinding.ActivityArcadeLevelsBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

public class ArcadeLevels extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "myLogs";

    //DBHelper dbhelper = new DBHelper(getApplicationContext());
    DBHelper dbHelper = new DBHelper(this);

    private ActivityArcadeLevelsBinding binding;

    LinearLayout llMain;
    NestedScrollView n;

    Bitmap a, b;
    Drawable accessibleLevel;
    Drawable inaccessibleLevel;

    static int SCREEN_WIDTH;
    static int SCREEN_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ArcadeLevels Activity");

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SCREEN_WIDTH = metrics.widthPixels;
        SCREEN_HEIGHT = metrics.heightPixels;

        binding = ActivityArcadeLevelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        //toolBarLayout.setTitle(getTitle());
        toolBarLayout.setTitle(getString(R.string.arcade_game));

        //toolBarLayout.setBackgroundResource(R.drawable.background);
        //toolbar.setBackgroundResource(R.drawable.background);

        n = findViewById(R.id.nested_scroll_view);

        if (MainActivity.sharedPrefs.getBoolean("isNightTheme", false)) {
            n.setBackgroundResource(R.drawable.background_black);
        } else {
            n.setBackgroundResource(R.drawable.background);
        }

        llMain = (LinearLayout) findViewById(R.id.linear_layout);

        a = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tile), SCREEN_WIDTH, (int) (getScreenHeight() / 10), true);
        accessibleLevel = new BitmapDrawable(getResources(), a); // если назвать "level_board" выдает ошибку после повторного захода в активити (???)

        b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.grey), SCREEN_WIDTH, (int) (getScreenHeight() / 10), true);
        inaccessibleLevel = new BitmapDrawable(getResources(), b); // если назвать "level_board_gray" выдает ошибку после повторного захода в активити (???)
        // если в названиях активной и неактивной плитки присутствуют повторяющиеся слова, выдает ошибку при повторном переходе к активити (???)

    }

    @Override
    protected void onResume() {
        super.onResume();

        llMain.removeAllViews();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("arcade_results", null, null, null, null, null, null);
        int[][] results = new int[cursor.getCount()][3];

        int counter = 0;
        int lastCompletedLevel = 0;
        int unfinishedLevels = 0;

        if (cursor.moveToFirst()) {
            int level = cursor.getColumnIndex("level");
            int record = cursor.getColumnIndex("record");
            int star = cursor.getColumnIndex("star");
            do {
                results[cursor.getInt(level) - 1][0] = cursor.getInt(level);
                results[cursor.getInt(level) - 1][1] = cursor.getInt(record);
                results[cursor.getInt(level) - 1][2] = cursor.getInt(star);
                if (cursor.getInt(star) > 0) {
                    lastCompletedLevel = cursor.getInt(level);
                    unfinishedLevels += counter;
                    counter = 0;
                } else {
                    counter++;
                }

            } while (cursor.moveToNext());
        }
        Log.d(TAG, "unfinishedLevels " + unfinishedLevels);

        for (int i = 0; i < cursor.getCount(); i++) {
            Log.d(TAG, " " + Arrays.toString(results[i]));
        }

        cursor.close();

        RelativeLayout[] relLayout = new RelativeLayout[results.length];
        TextView[] lvl = new TextView[results.length];
        TextView[] record = new TextView[results.length];
        TextView[] stars = new TextView[results.length];

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params1.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params4.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        for (int i = 1; i < results.length + 1; ++i) {
            relLayout[i-1] = new RelativeLayout(this);
            lvl[i-1] = new TextView(this);
            record[i-1] = new TextView(this);
            stars[i-1] = new TextView(this);

            if (lastCompletedLevel - i >= -3 + unfinishedLevels) {
                lvl[i-1].setText("    " + "  ".substring((String.valueOf(i).length() - 1) * 2) + i);
                lvl[i-1].setTextSize(spToPx(10, this));
                lvl[i-1].setTextColor(Color.rgb(0,0,0));
                record[i-1].setText(String.format("%0" + (5 - String.valueOf(results[i - 1][1]).length()) + "d", 0).replaceAll("[0]", "  ") + results[i - 1][1] + " ");
                record[i-1].setTextSize(spToPx(10, this));
                record[i-1].setTextColor(Color.rgb(0,0,0));
                stars[i-1].setText(String.format("%0" + (String.valueOf(results[i-1][2]).length()) + "d", 0).replaceAll("[0]", "★").substring(1) + String.format("%0" + (5 - String.valueOf(results[i-1][2]).length()) + "d", 0).replaceAll("[0]", "☆").substring(1) + "    ");
                stars[i-1].setTextSize(spToPx(10, this));
                stars[i-1].setTextColor(Color.rgb(0,0,0));

                lvl[i-1].setLayoutParams(params1);
                record[i-1].setLayoutParams(params2);
                stars[i-1].setLayoutParams(params3);

                relLayout[i-1].setId(i);
                //relLayout[i-1].setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                relLayout[i-1].addView(lvl[i-1]);
                relLayout[i-1].addView(record[i-1]);
                relLayout[i-1].addView(stars[i-1]);
                relLayout[i-1].setBackground(accessibleLevel);
                relLayout[i-1].setPadding(spToPx(20, this),0,spToPx(20, this),0);
                relLayout[i-1].setOnClickListener(this);

            } else {
                record[i-1].setText("\uD83D\uDD12");
                record[i-1].setTextSize(spToPx(10, this));
                record[i-1].setTextColor(Color.rgb(0,0,0));
                record[i-1].setLayoutParams(params4);
                relLayout[i-1].addView(record[i-1]);
                relLayout[i-1].setBackground(inaccessibleLevel);
            }
            llMain.addView(relLayout[i-1]);
        }
    }

    @Override
    public void onClick(View view) {
        //Log.d(TAG, "SCREEN_HEIGHT / SCREEN_WIDTH: " + (float) SCREEN_HEIGHT / SCREEN_WIDTH );

        Intent intent = new Intent(this, ArcadeGame.class);
        intent.putExtra("level", view.getId());
        startActivity(intent);
    }

    public static int spToPx(float sp, Context context) { // конвертирует sp в пиксели
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

}
