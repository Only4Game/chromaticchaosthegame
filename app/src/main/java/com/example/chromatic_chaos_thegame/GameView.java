package com.example.chromatic_chaos_thegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private List<int[]> colorPalettes;
    private int currentPaletteIndex = 0;
    private static final int SCORE_THRESHOLD = 50;
    private int[] currentPalette;

    public interface GameManager {
        void onGameOver(long finalScore);
        void addScore(int points);
        long getScore();
        void onPaletteChanged(int[] newColors);
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
        initializePalettes();
        currentPalette = colorPalettes.get(currentPaletteIndex);
        platformColor = currentPalette[0];
    }

    private void initializePalettes() {
        colorPalettes = new ArrayList<>();
        colorPalettes.add(new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW});
        colorPalettes.add(new int[]{Color.rgb(255, 182, 193), Color.rgb(144, 238, 144), Color.rgb(173, 216, 230), Color.rgb(255, 255, 224)});
        colorPalettes.add(new int[]{Color.MAGENTA, Color.CYAN, Color.rgb(0, 255, 0), Color.rgb(255, 165, 0)});
        colorPalettes.add(new int[]{Color.rgb(255, 20, 147), Color.rgb(57, 255, 20), Color.rgb(0, 191, 255), Color.rgb(255, 215, 0)});
    }

    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();
        int platformY = screenHeight - platformHeight - 200; // Podniesione wyżej dla przycisków
        int platformX = (screenWidth - platformWidth) / 2;
        platform = new Rect(platformX, platformY, platformX + platformWidth, platformY + platformHeight);
        startGame();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        int platformY = screenHeight - platformHeight - 200; // Podniesione wyżej
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
                gameManager.addScore(- (int)gameManager.getScore()); // Resetuj wynik
            }
            score = 0; // Resetuj lokalny wynik

            currentPaletteIndex = 0;
            currentPalette = colorPalettes.get(currentPaletteIndex);
            platformColor = currentPalette[0];
            if (gameManager != null) {
                gameManager.onPaletteChanged(currentPalette);
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
                    objectsToRemove.add(obj);
                }
                else if (obj.getRect().top > screenHeight) {
                    if (obj.getType() == FallingObject.ObjectType.NORMAL) {
                        // gameOver(); // Opcjonalnie: Koniec gry po ominięciu
                    }
                    objectsToRemove.add(obj);
                }
            }
            fallingObjects.removeAll(objectsToRemove);
        }
    }

    private void handleCollision(FallingObject obj) {
        FallingObject.ObjectType type = obj.getType();
        boolean scoreChanged = false;
        int pointsToAdd = 0;
        long currentManagerScore = (gameManager != null) ? gameManager.getScore() : score;


        switch (type) {
            case BOMB:
                gameOver();
                return;
            case BONUS_POINTS:
                pointsToAdd = 2;
                scoreChanged = true;
                break;
            case BONUS_SLOW:
                applySlowdown();
                break;
            case NORMAL:
                if (obj.getColor() == platformColor) {
                    pointsToAdd = 1;
                    scoreChanged = true;
                } else {
                    gameOver();
                    return;
                }
                break;
        }

        if (scoreChanged) {
            score += pointsToAdd; // Aktualizuj lokalny wynik
            if (gameManager != null) {
                gameManager.addScore(pointsToAdd);
            }

            // Sprawdź próg używając ZAKTUALIZOWANEGO wyniku
            if ((score / SCORE_THRESHOLD) > (currentManagerScore / SCORE_THRESHOLD)) {
                changePalette();
            }
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

    // NOWA METODA ZMIANY PALETY
    private void changePalette() {
        currentPaletteIndex = (currentPaletteIndex + 1) % colorPalettes.size();
        currentPalette = colorPalettes.get(currentPaletteIndex);
        platformColor = currentPalette[0]; // Ustaw domyślny kolor platformy z nowej palety

        if (gameManager != null) {
            gameManager.onPaletteChanged(currentPalette);
        }
    }

    private void spawnFallingObject() {
        int objectWidth = 80;
        int objectHeight = 80;
        int x = random.nextInt(screenWidth - objectWidth);
        int color = currentPalette[random.nextInt(currentPalette.length)];
        FallingObject.ObjectType type = FallingObject.ObjectType.NORMAL;
        int typeRoll = random.nextInt(100);
        if (typeRoll < 10) {
            type = FallingObject.ObjectType.BOMB;
            color = Color.BLACK;
        } else if (typeRoll < 15) {
            type = FallingObject.ObjectType.BONUS_POINTS;
            color = Color.rgb(255, 215, 0);
        } else if (typeRoll < 20) {
            type = FallingObject.ObjectType.BONUS_SLOW;
            color = Color.rgb(0, 191, 255);
        }
        FallingObject newObject = new FallingObject(x, -objectHeight, objectWidth, objectHeight, color, type);
        synchronized (fallingObjects) {
            fallingObjects.add(newObject);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.rgb(30, 30, 45));
        paint.setColor(platformColor);
        RectF platformRectF = new RectF(platform);
        float cornerRadius = 25f;
        canvas.drawRoundRect(platformRectF, cornerRadius, cornerRadius, paint);
        synchronized (fallingObjects) {
            for (FallingObject obj : fallingObjects) {
                obj.draw(canvas, paint);
            }
        }
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(score), screenWidth / 2f, 120, paint); // Użyj lokalnego 'score'
    }

    public void changePlatformColor(int color) {
        if (isPlaying) {
            platformColor = color;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPlaying) { return super.onTouchEvent(event); }
        int action = event.getActionMasked();
        float touchX = event.getX();
        float touchY = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (platform.contains((int) touchX, (int) touchY)) {
                    isPlatformDragging = true;
                    dragOffsetX = touchX - platform.left;
                    return true;
                } break;
            case MotionEvent.ACTION_MOVE:
                if (isPlatformDragging) {
                    float newLeft = touchX - dragOffsetX;
                    float newRight = newLeft + platformWidth;
                    if (newLeft < 0) { newLeft = 0; newRight = platformWidth; }
                    if (newRight > screenWidth) { newRight = screenWidth; newLeft = screenWidth - platformWidth; }
                    platform.left = (int) newLeft; platform.right = (int) newRight;
                    return true;
                } break;
            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                if (isPlatformDragging) { isPlatformDragging = false; return true; } break;
        }
        return super.onTouchEvent(event);
    }

    private void gameOver() {
        if (!isPlaying) return;
        isPlaying = false;
        handler.post(() -> {
            if (gameManager != null) {
                gameManager.onGameOver(score); // Przekaż lokalny 'score'
            }
        });
    }

    public void resume() {
        // Metoda może być pusta lub użyta do wznowienia, jeśli zaimplementujesz pauzę
    }

    public void pause() {
        stopGame();
    }
}