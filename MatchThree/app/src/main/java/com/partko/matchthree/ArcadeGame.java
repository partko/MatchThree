package com.partko.matchthree;

import static java.lang.Math.abs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class ArcadeGame extends AppCompatActivity {

    private static final String TAG = "myLogs";

    DBHelper dbHelper;
    SQLiteDatabase arcadeDB;
    SQLiteDatabase arcadeResultsDB;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    int levelNumber;
    int levelMoves;
    int levelGoal1;
    int levelGoal2;
    int levelGoal3;

    int numberOfStars = 0;

    boolean isMove = true; // флаг продолжается ли ход
    boolean isGame = true; // флаг закончена ли игра

    int record; // рекорд уровня
    int star; // количество звезд

    private static final Integer[] elements = {R.drawable.blue, R.drawable.green,
            R.drawable.orange, R.drawable.purple,
            R.drawable.red, R.drawable.yellow,
            R.drawable.light_green, R.drawable.violet, R.drawable.box};

    boolean isLevelGoal1 = false;
    boolean isLevelGoal2 = false;
    boolean isLevelGoal3 = false;

    private SoundPool mSoundPool;

    private float k; // коэффициент для учета соотношения сторон экрана

    GameLogicA a = new GameLogicA();

    private String boxString;

    private boolean isNoMoreMatches = false;
    private boolean isBack = false;
    private boolean straightMove = false;
    private boolean reverseMove = false;

    long startTime;
    long elapsedTime;
    int framesPerSecond = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ArcadeGame Activity");

        //---------------- загрузка звуков в SoundPool ----------------//
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        mSoundPool.load(this, R.raw.gem_move, 1);
        mSoundPool.load(this, R.raw.get_star, 1);
        mSoundPool.load(this, R.raw.lose, 1);
        mSoundPool.load(this, R.raw.win, 1);
        mSoundPool.load(this, R.raw.box, 1);

        //---------------- получение данных о экране ----------------//
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SCREEN_WIDTH = metrics.widthPixels;
        SCREEN_HEIGHT = metrics.heightPixels;
        Log.d(TAG, "SCREEN_WIDTH: " + SCREEN_WIDTH);
        Log.d(TAG, "SCREEN_HEIGHT: " + SCREEN_HEIGHT);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        framesPerSecond = ((int) display.getRefreshRate() <= 120 && (int) display.getRefreshRate() >= 60) ? (int) display.getRefreshRate() : 60;
        Log.d(TAG, "refreshRating = " + framesPerSecond);

        k = (float) (SCREEN_HEIGHT - 0.28 * SCREEN_WIDTH - 1.12 * SCREEN_WIDTH) / 2 - (float) (SCREEN_WIDTH * 0.03);
        Log.d(TAG, "k = " + k);

        //setContentView(R.layout.activity_arcade_game_ws);
        setContentView(new DrawView(this));

        Intent intent = getIntent();
        levelNumber = intent.getIntExtra("level", 0);

        //---------------- загрузка данных об уровне из БД ----------------//
        dbHelper = new DBHelper(this);
        DBHelper dbhelper = new DBHelper(getApplicationContext());
        arcadeDB = dbhelper.getWritableDatabase();

        Log.d(TAG, "Версия БД: " + arcadeDB.getVersion());

        Cursor cursor = arcadeDB.query("arcade", null, null, null, null, null, null);
        //Log.d(TAG, "" + cursor);

        arcadeResultsDB = dbhelper.getWritableDatabase();
        Cursor cursor2 = arcadeDB.query("arcade_results", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int lNumber = cursor.getColumnIndex(DBHelper.KEY_LEVEL);
            int levelType = cursor.getColumnIndex(DBHelper.KEY_LEVEL_TYPE);
            int moves = cursor.getColumnIndex(DBHelper.KEY_MOVES);
            int goalType = cursor.getColumnIndex(DBHelper.KEY_GOAL_TYPE);
            int goal1 = cursor.getColumnIndex(DBHelper.KEY_GOAL_1);
            int goal2 = cursor.getColumnIndex(DBHelper.KEY_GOAL_2);
            int goal3 = cursor.getColumnIndex(DBHelper.KEY_GOAL_3);
            int boxes = cursor.getColumnIndex(DBHelper.KEY_BOXES);
            do {
                if (cursor.getInt(lNumber) == levelNumber) {
                    levelMoves = cursor.getInt(moves);
                    levelGoal1 = cursor.getInt(goal1);
                    levelGoal2 = cursor.getInt(goal2);
                    levelGoal3 = cursor.getInt(goal3);
                    boxString = cursor.getString(boxes);
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            a.boxMatrix[j][i] = Character.getNumericValue(boxString.charAt(9*i + j));
                        }
                    }
                }
                Log.d(TAG, "levelNumber = " + cursor.getInt(lNumber) +
                        ", levelType = " + cursor.getInt(levelType) +
                        ", moves = " + cursor.getInt(moves) +
                        ", goalType = " + cursor.getInt(goalType) +
                        ", goal1 = " + cursor.getInt(goal1) +
                        ", goal2 = " + cursor.getInt(goal2) +
                        ", goal3 = " + cursor.getInt(goal3));

            } while (cursor.moveToNext());
        } else Log.d(TAG,"0 rows");

        cursor.close(); // закрытие cursor для освобождения памяти

        a.newMatrix();

        if (cursor2.moveToFirst()) {
            int lNumber = cursor2.getColumnIndex("level");
            int lRecord = cursor2.getColumnIndex("record");
            int lStar = cursor2.getColumnIndex("star");
            do {
                if (cursor2.getInt(lNumber) == levelNumber) {
                    record = cursor2.getInt(lRecord);
                    star = cursor2.getInt(lStar);
                }
                Log.d(TAG, "levelNumber = " + cursor2.getInt(lNumber) +
                        ", levelRecord = " + cursor2.getInt(lRecord) +
                        ", levelRecordStar = " + cursor2.getInt(lStar));

            } while (cursor2.moveToNext());
        } else Log.d(TAG,"0 rows");

        cursor2.close(); // закрытие cursor для освобождения памяти
    }

    class DrawView extends View {

        float timeCounter = 0.0f;

        Bitmap bitmap;
        Bitmap[] images = new Bitmap[9];
        Bitmap background;
        Bitmap hangingBoard;
        Bitmap signboard;
        Bitmap board;
        Bitmap roundBoard;
        Bitmap progressbar;
        Bitmap emptyStar;
        Bitmap fullStar;

        public DrawView(Context context) {
            super(context);

            for (int i = 0; i < elements.length; i++) {
                Bitmap image = BitmapFactory.decodeResource(getResources(), elements[i]);
                bitmap = Bitmap.createScaledBitmap(image, (int) (SCREEN_WIDTH * 0.12), (int) (SCREEN_WIDTH * 0.12), false);
                images[i] = bitmap;
                image.recycle();
            }

            if (MainActivity.sharedPrefs.getBoolean("isNightTheme", false)) {
                background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background_black), (int) (SCREEN_WIDTH), (int) (SCREEN_HEIGHT), false);
            } else {
                background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background), (int) (SCREEN_WIDTH), (int) (SCREEN_HEIGHT), false);
            }

            hangingBoard = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hanging_board), (int) (SCREEN_WIDTH * 0.4), (int) (SCREEN_WIDTH * 0.3), false);
            signboard = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.signboard), (int) (SCREEN_WIDTH * 0.45), (int) (SCREEN_WIDTH * 0.12), false);
            board = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.board), (int) (SCREEN_WIDTH * 0.96), (int) (SCREEN_WIDTH * 0.96), false);
            roundBoard = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.round_board), (int) (SCREEN_WIDTH * 0.16), (int) (SCREEN_WIDTH * 0.16), false);
            progressbar = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.progressbar), (int) (SCREEN_WIDTH), (int) (SCREEN_WIDTH * 0.12), false);
            emptyStar = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.empty_star), (int) (SCREEN_WIDTH * 0.06), (int) (SCREEN_WIDTH * 0.06), false);
            fullStar = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.red_star), (int) (SCREEN_WIDTH * 0.06), (int) (SCREEN_WIDTH * 0.06), false);
            //postInvalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Log.d(TAG, "Вызван onDraw(), timeCounter = " + timeCounter);

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(SCREEN_WIDTH / 16);
            canvas.drawPaint(paint);

            Paint paintCenter = new Paint();
            paintCenter.setColor(Color.BLACK);
            paintCenter.setStyle(Paint.Style.FILL);
            paintCenter.setTextAlign(Paint.Align.CENTER);
            paintCenter.setTextSize(SCREEN_WIDTH / 16);
            canvas.drawPaint(paintCenter);

            Paint paintProgress = new Paint();
            paintProgress.setColor(Color.GREEN);
            paintProgress.setStyle(Paint.Style.FILL);
            paintProgress.setTextAlign(Paint.Align.CENTER);
            canvas.drawPaint(paintProgress);

            //canvas.drawColor(Color.GREEN);

            canvas.drawBitmap(background, 0, 0, null);
            canvas.drawBitmap(signboard, 0, (float) (SCREEN_HEIGHT * 0.01), null);
            if (k >= 0) canvas.drawBitmap(hangingBoard, (float) (SCREEN_WIDTH * 0.6), 0, null);
            else canvas.drawBitmap(hangingBoard, (float) (SCREEN_WIDTH * 0.6), k, null);
            canvas.drawBitmap(roundBoard, (float) (SCREEN_WIDTH * 0.42), (float) (SCREEN_WIDTH * 0.28 +k - SCREEN_WIDTH * 0.1), null);
            canvas.drawBitmap(progressbar, (float) (0), (float) (SCREEN_WIDTH * 0.28 +k), null);
            canvas.drawBitmap(board, (float) (SCREEN_WIDTH * 0.02), (float) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k), null);

            canvas.drawText("Уровень " + levelNumber, (float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.1), paint);
            if (k >= 0) canvas.drawText("" + a.score, (float) (SCREEN_WIDTH * 0.8), (float) (SCREEN_WIDTH * 0.24), paintCenter);
            else canvas.drawText("" + a.score, (float) (SCREEN_WIDTH * 0.8), (float) (SCREEN_WIDTH * 0.22 + k), paintCenter);
            canvas.drawText("" + levelMoves, (float) (SCREEN_WIDTH * 0.5), (float) (SCREEN_WIDTH * 0.28 +k), paintCenter);

            canvas.drawRect((float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.07),(float) (SCREEN_WIDTH * 0.95),(float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.06), paint);
            if (a.score <= levelGoal3){
                canvas.drawRect((float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.07),(float) ((float) a.score / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05),(float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.06), paintProgress);
            } else {
                canvas.drawRect((float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.07),(float) (SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05),(float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.06), paintProgress);
            }

            if (a.score >= levelGoal1) {
                canvas.drawBitmap(fullStar, (float) ((float) levelGoal1 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            } else {
                canvas.drawBitmap(emptyStar, (float) ((float) levelGoal1 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            }
            if (a.score >= levelGoal2) {
                canvas.drawBitmap(fullStar, (float) ((float) levelGoal2 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            } else {
                canvas.drawBitmap(emptyStar, (float) ((float) levelGoal2 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            }
            if (a.score >= levelGoal3) {
                canvas.drawBitmap(fullStar, (float) ((float) levelGoal3 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            } else {
                canvas.drawBitmap(emptyStar, (float) ((float) levelGoal3 / levelGoal3 * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05 - SCREEN_WIDTH * 0.03), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.03), null);
            }


            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {

                    if (i == posX1 && j == posY1 && (posX1 != posX2 || posY1 != posY2)) {
                        canvas.drawBitmap(images[a.matrix[posX2][posY2] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12 + 0.12 * timeCounter * (posX2 - posX1))), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * (j * 0.12 + 0.12 * timeCounter * (posY2 - posY1))), null);
                    } else if (i == posX2 && j == posY2 && (posX1 != posX2 || posY1 != posY2)) {
                        canvas.drawBitmap(images[a.matrix[posX1][posY1] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12 + 0.12 * timeCounter * (posX1 - posX2))), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * (j * 0.12 + 0.12 * timeCounter * (posY1 - posY2))), null);
                        Log.d(TAG, "Метка Анимация " + timeCounter);
                        //postInvalidateDelayed( 600 / framesPerSecond);

                        if (!isBack && straightMove) {
                            postInvalidateDelayed((long) (1 * ((float) framesPerSecond / 60)));
                            if (straightMove) {
                                timeCounter += 0.075;
                                if (timeCounter >= 1) {straightMove = false; timeCounter = 1;}
                                //Log.d(TAG, "OnlyStraightMove " + timeCounter);
                            }
                        } else {
                            if (isBack && (straightMove || reverseMove)) {
                                postInvalidateDelayed((long) (2 * ((float) framesPerSecond / 60)));
                                if (straightMove) {
                                    timeCounter -= 0.075;
                                    if (timeCounter <= 0) {straightMove = false; timeCounter = 0;}
                                    //Log.d(TAG, "straightMove " + timeCounter);
                                }
                                if (!straightMove && reverseMove) {
                                    timeCounter += 0.075;
                                    if (timeCounter >= 1) {reverseMove = false; timeCounter = 1; isBack = false;}
                                    //Log.d(TAG, "reverseMove " + timeCounter);
                                }
                            }
                        }

                    } else {
                        canvas.drawBitmap(images[a.matrix[i][j] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12)), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * j * 0.12), null);
                    }
                    //postInvalidateDelayed( 10000 / framesPerSecond);
                    //invalidate();
                }
            }
            //postInvalidateDelayed( 10000 / framesPerSecond);
        }

        @Override
        public void invalidate() {
            super.invalidate();
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private float startX, startY, finishX, finishY;
        private int posX1, posY1, posX2, posY2;
        private final float cellSize = (float) (SCREEN_WIDTH * 0.12);

        private void touch_start(float x, float y) {
            mX = x;
            mY = y;
            Log.d(TAG, "touch_start " + mX + " " + mY);
            if (mX >= SCREEN_WIDTH * 0.02 && mX <= SCREEN_WIDTH * 0.98 && mY >= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k && mY <= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * 0.96) {
                Log.d(TAG, "Касание попало в игровую область " + mX + " " + mY);
                startX = mX;
                startY = mY;
            }
        }

        private void touch_move(float x, float y) {
            float dX = abs(x - mX);
            float dY = abs(y - mY);
            if (dX >= TOUCH_TOLERANCE || dY >= TOUCH_TOLERANCE) {
                //Log.d(TAG, "touch_move " + dX + " " + dY);
                elapsedTime = System.currentTimeMillis() - startTime;
                Log.d(TAG, "elapsedTime = " + elapsedTime);

                if (elapsedTime > 350) { // задержка 350мс до следующего хода
                    if (isMove && (dX >= cellSize || dY >= cellSize) && (x >= SCREEN_WIDTH * 0.02 && x <= SCREEN_WIDTH * 0.98 && y >= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k && y <= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * 0.96)) {
                        startTime = System.currentTimeMillis();
                        Log.d(TAG, "startTime " + startTime);

                        finishX = x;
                        finishY = y;
                        cellAnalysis();
                        endGame();
                        isMove = false;
                    }
                }
            }
        }

        private void touch_up(float x, float y) {
            mX = x;
            mY = y;
            Log.d(TAG, "touch_up " + mX + " " + mY);
        }

        private void cellAnalysis(){
            posX1 = (int) ((startX - SCREEN_WIDTH * 0.02) / cellSize);
            posY1 = (int) ((startY - (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k)) / cellSize);
            posX2 = (int) ((finishX - SCREEN_WIDTH * 0.02) / cellSize);
            posY2 = (int) ((finishY - (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k)) / cellSize);
            Log.d(TAG, "cellAnalysis (" + posX1 + " " + posY1 + ") (" + posX2 + " " + posY2 + ")");
            //Log.d(TAG, "Cell Size: " + cellSize);
            if ((abs(posX1 - posX2) + abs(posY1 - posY2) == 1) && isGame && a.matrix[posX1][posY1] != 9 && a.matrix[posX2][posY2] != 9) {
                Log.d(TAG, "posX1: " + posX1);
                Log.d(TAG, "posY1: " + posY1);
                Log.d(TAG, "posX2: " + posX2);
                Log.d(TAG, "posY2: " + posY2);

                if (a.checkMatrix(posX1, posY1, posX2, posY2)) {
                    isBack = false;
                    straightMove = true;
                    invalidate();
                    timeCounter = 0.0f;
                    a.gemExchange(posX1, posY1, posX2, posY2);
                    levelMoves--;

                    playSound(1);

                    while(a.searchMatches(true)) {
                        a.boxes();
                        a.fallingGems();
                        a.gemReplacement();
                    }
                    invalidate();

                    if (a.isBoxCrack) {
                        playSound(5);
                        a.isBoxCrack = false;
                    }

                    //Log.d(TAG, "score: " + score);
                    //Log.d(TAG, "levelGoal1: " + levelGoal1);
                    if (a.score >= levelGoal1 && !isLevelGoal1) {
                        isLevelGoal1 = true;
                        playSound(2);
                    } else if (a.score >= levelGoal2 && !isLevelGoal2) {
                        isLevelGoal2 = true;
                        playSound(2);
                    } else if (a.score >= levelGoal3 && !isLevelGoal3) {
                        isLevelGoal3 = true;
                        playSound(2);
                    }

                    if (levelMoves == 0) {
                        isGame = false;
                    } else {
                        while (a.analysisForExchanges() == 0) {
                            if (!isNoMoreMatches) {
                                Toast.makeText(getApplicationContext(), "Совпадений не осталось!\nМеняем поле", Toast.LENGTH_SHORT).show();
                                isNoMoreMatches = true;
                            }
                            a.createMatrix();
                            while (a.searchMatches(false)) { // совпавшие в ряд 3+ элементы заменяются на отрицательные
                                a.gemReplacement(); // отрицательные элементы заменяются на новые случайные
                            }
                        }
                        isNoMoreMatches = false;
                        invalidate();
                    }
                } else {
                    playSound(1);
                    isBack = true;
                    straightMove = true;
                    reverseMove = true;
                    timeCounter = 1.0f;
                    invalidate();
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up(x, y);
                    isMove = true;
                    //invalidate();
                    break;
            }
            return true;
        }

        //---------------- итоги уровня ----------------//
        private void endGame() {
            if (!isGame && a.score >= levelGoal1) {

                playSound(4);

                if (a.score >= levelGoal3) {
                    numberOfStars = 1110;
                } else if (a.score >= levelGoal2) {
                    numberOfStars = 110;
                } else if (a.score >= levelGoal1) {
                    numberOfStars = 10;
                }

                if (a.score > record) {
                    ContentValues starCount = new ContentValues();
                    starCount.put("star", numberOfStars);
                    ContentValues highScore = new ContentValues();
                    highScore.put("record", a.score);

                    arcadeDB.update("arcade_results", starCount, String.format("%s = %d", "level", levelNumber), null);
                    arcadeDB.update("arcade_results", highScore, String.format("%s = %d", "level", levelNumber), null);

                    new AlertDialog.Builder(ArcadeGame.this)
                            .setTitle("Новый рекорд!")
                            .setMessage(record + " ---> " + a.score)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Intent intent = new Intent(ArcadeGame.this, ArcadeLevels.class);
                                    //startActivity(intent);
                                    finish();
                                }
                            })
                            //.setNegativeButton(android.R.string.no, null)
                            .setIcon(R.drawable.baseline_star_rate_black_24dp)
                            .show();
                } else {
                    new AlertDialog.Builder(ArcadeGame.this)
                            .setTitle("Уровень пройден!")
                            .setMessage("Счет: " + a.score)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Intent intent = new Intent(ArcadeGame.this, ArcadeLevels.class);
                                    //startActivity(intent);
                                    finish();
                                }
                            })
                            //.setNegativeButton(android.R.string.no, null)
                            .setIcon(R.drawable.baseline_done_black_24dp)
                            .show();
                }
            }

            if (!isGame && a.score < levelGoal1) {

                playSound(3);

                new AlertDialog.Builder(ArcadeGame.this)
                        .setTitle("Уровень не пройден")
                        .setMessage("Пробуйте еще")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Intent intent = new Intent(ArcadeGame.this, ArcadeLevels.class);
                                //startActivity(intent);
                                finish();
                            }
                        })
                        //.setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_delete)
                        .show();
            }
        }
    }

    protected void playSound(int id) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = curVolume / maxVolume * MainActivity.sharedPrefs.getInt("volume", 0) / 100;;
        float rightVolume = curVolume / maxVolume * MainActivity.sharedPrefs.getInt("volume", 0) / 100;;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        int mStreamId = mSoundPool.play(id, leftVolume, rightVolume, priority, no_loop, normal_playback_rate);
    }
}
