package com.partko.matchthree;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myLogs";

    public static String DBNAME = "levels.db";
    public static String DBLOCATION = "/data/data/com.partko.matchthree/databases/";
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static final String KEY_LEVEL = "level";
    public static final String KEY_LEVEL_TYPE = "ltype";
    public static final String KEY_MOVES = "moves";
    public static final String KEY_GOAL_TYPE = "gtype";
    public static final String KEY_GOAL_1 = "goal1";
    public static final String KEY_GOAL_2 = "goal2";
    public static final String KEY_GOAL_3 = "goal3";
    public static final String KEY_BOXES = "boxes";


    public DBHelper(Context context) {
        super(context, DBNAME, null, 1);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public static boolean copyDatabase(Context context) {
        try {
            InputStream inputStream = context.getAssets().open(DBHelper.DBNAME);
            String outFileName = DBHelper.DBLOCATION + DBHelper.DBNAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            Log.d(TAG, "БД скопирована");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "БД не скопирована");
            return false;
        }
    }
}
