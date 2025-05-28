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

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private volatile boolean isPlaying;
    private Paint paint;
    private int screenWidth;
    private int screenHeight;

    private Rect platform;
    private int platformWidth = 200;
    private int platformHeight = 50;
    private int platformColor = Color.RED;
    private final int[] availableColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};

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
    private boolean isPlatformDragging = false;
    private float dragOffsetX = 0;

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

        int platformY = screenHeight - platformHeight - 150;
        int platformX = (screenWidth - platformWidth) / 2;
        platform = new Rect(platformX, platformY, platformX + platformWidth, platformY + platformHeight);

        startGame();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        int platformY = screenHeight - platformHeight - 150;
        int currentPlatformLeft = platform.left;
        platform.set(currentPlatformLeft, platformY, currentPlatformLeft + platformWidth, platformY + platformHeight);
        if (platform.right > screenWidth) {
            platform.right = screenWidth;
            platform.left = screenWidth - platformWidth;
        }
        if (platform.left < 0) {
            platform.left = 0;
            platform.right = platformWidth;
        }
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
            int platformX = (screenWidth - platformWidth) / 2;
            platform.offsetTo(platformX, platform.top);
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

    private void update() {
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
                    objectsToRemove.add(obj); // Zawsze usuwamy obiekt po kolizji
                }
                else if (obj.getRect().top > screenHeight) {
                    // Sprawdź, czy pominięty obiekt jest NORMALNY
                    if (obj.getType() == FallingObject.ObjectType.NORMAL) {
                        // Jeśli pominięto normalny obiekt, zakończ grę
                        // (Można to zmienić na utratę życia, jeśli chcesz)
                        // gameOver(); // ODZNACZ, JEŚLI CHCESZ, ABY POMINIĘCIE KOŃCZYŁO GRĘ
                    }
                    objectsToRemove.add(obj); // Usuwamy obiekty, które spadły poza ekran
                }
            }
            fallingObjects.removeAll(objectsToRemove);
        }
    }

    // ZMODYFIKOWANA METODA handleCollision
    private void handleCollision(FallingObject obj) {
        FallingObject.ObjectType type = obj.getType();

        switch (type) {
            case BOMB:
                // Bomba zawsze kończy grę, niezależnie od koloru
                gameOver();
                break;

            case BONUS_POINTS:
                // Bonus punktowy zawsze dodaje punkty i nie kończy gry
                if (gameManager != null) gameManager.addScore(2);
                else score += 2;
                break;

            case BONUS_SLOW:
                // Bonus spowolnienia zawsze spowalnia i nie kończy gry
                applySlowdown();
                break;

            case NORMAL:
                // Normalny obiekt sprawdza kolor
                if (obj.getColor() == platformColor) {
                    // Dobry kolor - dodaj punkt
                    if (gameManager != null) gameManager.addScore(1);
                    else score++;
                } else {
                    // Zły kolor - koniec gry
                    gameOver();
                }
                break;
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

    public void changePlatformColor(int color) {
        if (isPlaying) {
            platformColor = color;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPlaying) {
            return super.onTouchEvent(event);
        }

        int action = event.getActionMasked();
        float touchX = event.getX();
        float touchY = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (platform.contains((int) touchX, (int) touchY)) {
                    isPlatformDragging = true;
                    dragOffsetX = touchX - platform.left;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isPlatformDragging) {
                    float newLeft = touchX - dragOffsetX;
                    float newRight = newLeft + platformWidth;

                    if (newLeft < 0) {
                        newLeft = 0;
                        newRight = platformWidth;
                    }
                    if (newRight > screenWidth) {
                        newRight = screenWidth;
                        newLeft = screenWidth - platformWidth;
                    }

                    platform.left = (int) newLeft;
                    platform.right = (int) newRight;

                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isPlatformDragging) {
                    isPlatformDragging = false;
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void gameOver() {
        if (!isPlaying) return; // Zapobiegaj wielokrotnemu wywołaniu gameOver

        isPlaying = false;
        handler.post(() -> {
            if (gameManager != null) {
                gameManager.onGameOver(gameManager.getScore());
            }
        });
    }

    public void resume() { }

    public void pause() {
        stopGame();
    }
}