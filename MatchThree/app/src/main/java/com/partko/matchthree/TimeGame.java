package com.partko.matchthree;

import static java.lang.Math.abs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class TimeGame extends AppCompatActivity {

    private static final String TAG = "myLogs";

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    boolean isMove = true; // флаг продолжается ли ход
    boolean isGame = true; // флаг закончена ли игра

    int record; // рекорд уровня

    final int TIME = 46;
    float time = 46;
    float timeStop;

    private static final Integer[] gems = {R.drawable.blue, R.drawable.green,
            R.drawable.orange, R.drawable.purple,
            R.drawable.red, R.drawable.yellow,
            R.drawable.light_green, R.drawable.violet};


    private SoundPool mSoundPool;

    private float k; // коэффициент для учета соотношения сторон экрана

    GameLogicT a = new GameLogicT();

    Timer myTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TimeGame Activity");

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

        k = (float) (SCREEN_HEIGHT - 0.28 * SCREEN_WIDTH - 1.12 * SCREEN_WIDTH) / 2 - (float) (SCREEN_WIDTH * 0.03);
        Log.d(TAG, "k = " + k);

        //setContentView(R.layout.activity_time_game);
        setContentView(new DrawView(this));

        record = MainActivity.sharedPrefs.getInt("timeRecord", 0);
        Log.d(TAG, "timeRecord = " + MainActivity.sharedPrefs.getInt("timeRecord", 0));

        timeStop = TIME;
    }

    @Override
    public void onResume() {
        super.onResume();
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            public void run() {
                timerTick();
            }
        }, 0, 1000); // каждую 1 секунду
    }

    @Override
    public void onPause() {
        super.onPause();
        timeStop = time;
    }

    @Override
    public void onStop() {
        super.onStop();
        myTimer.cancel();
    }

    private void timerTick() {
        this.runOnUiThread(doTask);
    }

    private Runnable doTask = new Runnable() {
        public void run() {
            if (time > 0) time--;
            Log.d(TAG, "Time: " + time);
            //DrawView.invalidate();
            if (time == 0) {myTimer.cancel(); isGame = false; endGame(); time = 10;}
        }
    };

    class DrawView extends View {

        int framesPerSecond = 60;
        float timeCounter = 0.0f;

        Bitmap bitmap;
        Bitmap[] images = new Bitmap[8];
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

            for (int i = 0; i < gems.length; i++) {
                Bitmap image = BitmapFactory.decodeResource(getResources(), gems[i]);
                bitmap = Bitmap.createScaledBitmap(
                        image, (int) (SCREEN_WIDTH * 0.12), (int) (SCREEN_WIDTH * 0.12), false);
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


            canvas.drawText("Рекорд " + record, (float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.1), paint);
            if (k >= 0) canvas.drawText("" + a.score, (float) (SCREEN_WIDTH * 0.8), (float) (SCREEN_WIDTH * 0.24), paintCenter);
            else canvas.drawText("" + a.score, (float) (SCREEN_WIDTH * 0.8), (float) (SCREEN_WIDTH * 0.22 + k), paintCenter);

            if (isGame) canvas.drawText("" + (int) time, (float) (SCREEN_WIDTH * 0.5), (float) (SCREEN_WIDTH * 0.28 +k), paintCenter);
            else canvas.drawText("0", (float) (SCREEN_WIDTH * 0.5), (float) (SCREEN_WIDTH * 0.28 +k), paintCenter);

            canvas.drawRect((float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.07),(float) (SCREEN_WIDTH * 0.95),(float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.06), paint);
            if (time >= 0 && isGame){
                canvas.drawRect((float) (SCREEN_WIDTH * 0.05), (float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.07),(float) ((float) (time+1) / TIME * SCREEN_WIDTH * 0.9 + SCREEN_WIDTH * 0.05),(float) (SCREEN_WIDTH * 0.28 +k + SCREEN_WIDTH * 0.06), paintProgress);
            }


            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {

                    if (i == posX1 && j == posY1 && (posX1 != posX2 || posY1 != posY2) && (abs(posX1 - posX2) + abs(posY1 - posY2) == 1)) {
                        canvas.drawBitmap(images[a.matrix[posX2][posY2] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12 + 0.12 * timeCounter * (posX2 - posX1))), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * (j * 0.12 + 0.12 * timeCounter * (posY2 - posY1))), null);
                    } else if (i == posX2 && j == posY2 && (posX1 != posX2 || posY1 != posY2) && (abs(posX1 - posX2) + abs(posY1 - posY2) == 1)) {
                        canvas.drawBitmap(images[a.matrix[posX1][posY1] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12 + 0.12 * timeCounter * (posX1 - posX2))), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * (j * 0.12 + 0.12 * timeCounter * (posY1 - posY2))), null);
                        Log.d(TAG, "Метка Анимация " + timeCounter);
                        //postInvalidateDelayed( 600 / framesPerSecond);
                        if (timeCounter <= 1) {
                            postInvalidateDelayed( 60 / framesPerSecond);
                            timeCounter += 0.1;
                        }
                    } else {
                        canvas.drawBitmap(images[a.matrix[i][j] - 1], (int) (SCREEN_WIDTH * (0.02 + i * 0.12)), (int) (SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * j * 0.12), null);
                    }

                    //postInvalidateDelayed( 10000 / framesPerSecond);
                    //canvas.drawBitmap(bitmap, 40 + counter, 80, null);
                    //invalidate();
                }
            }
            //postInvalidateDelayed( 10000 / framesPerSecond);
            if (time == timeStop && isGame) { postInvalidateDelayed(0); }
            Log.d(TAG, "timeStop, time " + timeStop + " " + time);
            if (time > 0  && isGame) { postInvalidateDelayed(1000); }
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
            float dx = abs(x - mX);
            float dy = abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                //Log.d(TAG, "touch_move " + mX + " " + mY);
                if (isMove && (dx >= cellSize || dy >= cellSize) && (x >= SCREEN_WIDTH * 0.02 && x <= SCREEN_WIDTH * 0.98 && y >= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k && y <= SCREEN_WIDTH * 0.28 + SCREEN_WIDTH * 0.12 +k + SCREEN_WIDTH * 0.96)) {
                    finishX = x;
                    finishY = y;
                    cellAnalysis();
                    isMove = false;
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
            if ((abs(posX1 - posX2) + abs(posY1 - posY2) == 1) && isGame) {
                Log.d(TAG, "posX1: " + posX1);
                Log.d(TAG, "posY1: " + posY1);
                Log.d(TAG, "posX2: " + posX2);
                Log.d(TAG, "posY2: " + posY2);

                invalidate();
                timeCounter = 0.0f;

                a.gemExchange(posX1, posY1, posX2, posY2);

                playSound(1);

                while(a.searchMatches(true)) {
                    a.fallingGems();
                    a.gemReplacement();
                }
                invalidate();

                while (a.analysisForExchanges() == 0) {
                    Toast.makeText(getApplicationContext(), "Совпадений не осталось!\nМеняем поле", Toast.LENGTH_SHORT).show();
                    a.createMatrix();
                    while (a.searchMatches(false)) { // совпавшие в ряд 3+ элементы заменяются на отрицательные
                        a.gemReplacement(); // отрицательные элементы заменяются на новые случайные
                    }
                }
                invalidate();
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
    }

    //---------------- итоги уровня ----------------//
    private void endGame() {
        if (!isGame) {

            playSound(4);

            if (a.score > record) {
                SharedPreferences.Editor ed = MainActivity.sharedPrefs.edit();
                ed.putInt("timeRecord", a.score);
                ed.apply();

                new AlertDialog.Builder(TimeGame.this)
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
                new AlertDialog.Builder(TimeGame.this)
                        .setTitle("Рекорд не побит!")
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
