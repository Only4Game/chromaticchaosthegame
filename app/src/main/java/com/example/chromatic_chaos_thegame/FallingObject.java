package com.example.chromatic_chaos_thegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path; // Do rysowania niestandardowych kształtów (gwiazda)
import android.graphics.Rect;
import android.graphics.RectF; // Do zaokrąglonych prostokątów

public class FallingObject {

    public enum ObjectType {
        NORMAL,
        BOMB,
        BONUS_POINTS,
        BONUS_SLOW
    }

    private Rect rect;
    private int color;
    private ObjectType type;
    private int width;
    private int height;
    private RectF rectF; // Do zaokrąglonych prostokątów

    public FallingObject(int x, int y, int width, int height, int color, ObjectType type) {
        this.width = width;
        this.height = height;
        this.rect = new Rect(x, y, x + width, y + height);
        this.rectF = new RectF(this.rect); // Inicjalizuj RectF
        this.color = color;
        this.type = type;
    }

    public void update(float speed) {
        rect.top += speed;
        rect.bottom += speed;
        rectF.top += speed; // Aktualizuj też RectF
        rectF.bottom += speed;
    }

    // ZMODYFIKOWANA METODA draw
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(this.color);
        paint.setStyle(Paint.Style.FILL);
        float cornerRadius = 20f; // Promień zaokrąglenia dla normalnych obiektów
        float cx = rect.exactCenterX();
        float cy = rect.exactCenterY();
        float radius = width / 2f;

        switch (type) {
            case BOMB:
                paint.setColor(Color.BLACK); // Czarna bomba
                canvas.drawCircle(cx, cy, radius, paint);
                paint.setColor(Color.GRAY); // Szary połysk
                canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.3f, radius * 0.2f, paint);
                paint.setColor(Color.WHITE); // Biały "lont"
                paint.setStrokeWidth(8);
                canvas.drawLine(cx, cy - radius, cx + 5, cy - radius - 20, paint);
                paint.setColor(Color.RED);
                canvas.drawCircle(cx+5, cy - radius - 20, 5, paint);
                break;

            case BONUS_POINTS:
                paint.setColor(this.color); // Użyj koloru bonusu (np. złoty)
                drawStar(canvas, paint, cx, cy, radius, 5); // Narysuj 5-ramienną gwiazdę
                break;

            case BONUS_SLOW:
                paint.setColor(this.color); // Użyj koloru bonusu (np. niebieski)
                canvas.drawCircle(cx, cy, radius, paint);
                paint.setColor(Color.WHITE); // Rysuj wskazówki zegara
                paint.setStrokeWidth(6);
                canvas.drawLine(cx, cy, cx, cy - radius * 0.7f, paint); // Wskazówka minutowa
                canvas.drawLine(cx, cy, cx + radius * 0.5f, cy, paint); // Wskazówka godzinowa
                break;

            case NORMAL:
            default:
                paint.setColor(this.color); // Użyj koloru z palety
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
                break;
        }
    }

    // Pomocnicza metoda do rysowania gwiazdy
    private void drawStar(Canvas canvas, Paint paint, float centerX, float centerY, float outerRadius, int numPoints) {
        float innerRadius = outerRadius / 2.5f;
        Path path = new Path();
        float angle = (float) (Math.PI / numPoints);
        float rotation = (float) (Math.PI / 2f * 3f); // Obróć, aby stała prosto

        path.moveTo(centerX + outerRadius * (float) Math.cos(rotation),
                centerY + outerRadius * (float) Math.sin(rotation));

        for (int i = 1; i < numPoints * 2; i++) {
            float r = (i % 2) == 0 ? outerRadius : innerRadius;
            float currentAngle = i * angle + rotation;
            path.lineTo(centerX + r * (float) Math.cos(currentAngle),
                    centerY + r * (float) Math.sin(currentAngle));
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    public Rect getRect() { return rect; }
    public int getColor() { return color; }
    public ObjectType getType() { return type; }
}