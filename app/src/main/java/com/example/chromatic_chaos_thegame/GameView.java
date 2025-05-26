package com.example.chromatic_chaos_thegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.view.MotionEvent;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private volatile boolean isPlaying;
    private Paint paint;
    private int screenWidth;
    private int screenHeight;

    private Rect platform;

    private float platformX; // aktualna pozycja X środka platformy

    private int platformWidth = 200;
    private int platformHeight = 50;
    private int platformColor = Color.RED;
    private final int[] availableColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
    private int currentColorIndex = 0;

    private List<FallingObject> fallingObjects;
    private Random random;
    private Handler handler;

    private long lastObjectSpawnTime;
    private long spawnInterval = 1500;
    private float objectSpeed = 5f;
    private long score = 0;
    private long gameStartTime;
    private long speedIncreaseInterval = 10000;
    private long lastSpeedIncreaseTime;

    private GameManager gameManager;

    public interface GameManager {
        void onGameOver(long finalScore);
        void addScore(int points);
        long getScore();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
        fallingObjects = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());
        lastObjectSpawnTime = System.currentTimeMillis();
        gameStartTime = System.currentTimeMillis();
        lastSpeedIncreaseTime = gameStartTime;
    }

    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        int platformY = screenHeight - platformHeight - 100;
        platformX = screenWidth / 2f;
        platform = new Rect((int)(platformX - platformWidth / 2), platformY, (int)(platformX + platformWidth / 2), platformY + platformHeight);


        startGame();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        int platformY = screenHeight - platformHeight - 100;
        platformX = screenWidth / 2f;
        platform = new Rect((int)(platformX - platformWidth / 2), platformY, (int)(platformX + platformWidth / 2), platformY + platformHeight);

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopGame();
    }

    private void startGame() {
        if (gameThread == null || !gameThread.isAlive()) {
            isPlaying = true;
            gameThread = new Thread(this);
            gameThread.start();
            if (gameManager != null) {
                score = gameManager.getScore();
            } else {
                score = 0;
            }
            gameStartTime = System.currentTimeMillis();
            lastSpeedIncreaseTime = gameStartTime;
            lastObjectSpawnTime = gameStartTime;
            fallingObjects.clear();
        }
    }

    public void stopGame() {
        isPlaying = false;
        boolean retry = true;
        while (retry) {
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        gameThread = null;
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            update();
                            draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                platformX = event.getX();
                break;
        }
        return true;
    }

    private void update() {
        int platformY = screenHeight - platformHeight - 100;
        platform.set((int)(platformX - platformWidth / 2), platformY, (int)(platformX + platformWidth / 2), platformY + platformHeight);


        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSpeedIncreaseTime > speedIncreaseInterval) {
            objectSpeed += 1.0f;
            spawnInterval = Math.max(300, spawnInterval - 100);
            lastSpeedIncreaseTime = currentTime;
        }

        if (currentTime - lastObjectSpawnTime > spawnInterval) {
            spawnFallingObject();
            lastObjectSpawnTime = currentTime;
        }

        List<FallingObject> objectsToRemove = new ArrayList<>();
        synchronized (fallingObjects) {
            for (FallingObject obj : fallingObjects) {
                obj.update(objectSpeed);

                if (Rect.intersects(obj.getRect(), platform)) {
                    handleCollision(obj);
                    objectsToRemove.add(obj);
                }
                else if (obj.getRect().top > screenHeight) {
                    if(obj.getType() == FallingObject.ObjectType.NORMAL){
                        // Missed normal object - optional penalty
                    }
                    objectsToRemove.add(obj);
                }
            }
            fallingObjects.removeAll(objectsToRemove);
        }
    }

    private void handleCollision(FallingObject obj) {
        if (obj.getType() == FallingObject.ObjectType.BOMB) {
            gameOver();
            return;
        }

        if (obj.getColor() == platformColor) {
            if (obj.getType() == FallingObject.ObjectType.BONUS_POINTS) {
                if (gameManager != null) gameManager.addScore(2);
                else score += 2;
            } else if (obj.getType() == FallingObject.ObjectType.BONUS_SLOW) {
                applySlowdown();
            } else {
                if (gameManager != null) gameManager.addScore(1);
                else score++;
            }
        } else {
            gameOver();
        }
    }

    private void applySlowdown() {
        final float originalSpeed = objectSpeed;
        final long originalSpawnInterval = spawnInterval;

        objectSpeed = Math.max(1f, objectSpeed * 0.5f);
        spawnInterval = Math.min(5000, spawnInterval + 500);

        handler.postDelayed(() -> {
            if (isPlaying) {
                objectSpeed = originalSpeed;
                spawnInterval = originalSpawnInterval;
            }
        }, 5000);
    }

    private void spawnFallingObject() {
        int objectWidth = 80;
        int objectHeight = 80;
        int x = random.nextInt(screenWidth - objectWidth);
        int color = availableColors[random.nextInt(availableColors.length)];

        FallingObject.ObjectType type = FallingObject.ObjectType.NORMAL;
        int typeRoll = random.nextInt(100);
        if (typeRoll < 10) {
            type = FallingObject.ObjectType.BOMB;
            color = Color.BLACK;
        } else if (typeRoll < 20) {
            int bonusType = random.nextInt(2);
            if (bonusType == 0) {
                type = FallingObject.ObjectType.BONUS_POINTS;
                color = Color.MAGENTA;
            } else {
                type = FallingObject.ObjectType.BONUS_SLOW;
                color = Color.CYAN;
            }
        }

        FallingObject newObject = new FallingObject(x, -objectHeight, objectWidth, objectHeight, color, type);
        synchronized (fallingObjects) {
            fallingObjects.add(newObject);
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.DKGRAY);

        paint.setColor(platformColor);
        canvas.drawRect(platform, paint);

        synchronized (fallingObjects) {
            for (FallingObject obj : fallingObjects) {
                obj.draw(canvas, paint);
            }
        }

        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.LEFT);
        long currentScore = (gameManager != null) ? gameManager.getScore() : score;
        canvas.drawText("Score: " + currentScore, 50, 100, paint);

        long elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Time: " + elapsedTime + "s", screenWidth - 50, 100, paint);
    }
    private void gameOver() {
        isPlaying = false;
        handler.post(() -> {
            if (gameManager != null) {
                gameManager.onGameOver(gameManager.getScore());
            }
        });
    }

    public void resume() {
        // Called via Activity/Fragment onResume
        // If needed, restart the game logic or thread here
    }

    public void pause() {
        stopGame();
    }
}
