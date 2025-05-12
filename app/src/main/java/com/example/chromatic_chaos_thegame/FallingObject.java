package com.example.chromatic_chaos_thegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

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

    public FallingObject(int x, int y, int width, int height, int color, ObjectType type) {
        this.width = width;
        this.height = height;
        this.rect = new Rect(x, y, x + width, y + height);
        this.color = color;
        this.type = type;
    }

    public void update(float speed) {
        rect.top += speed;
        rect.bottom += speed;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(this.color);
        paint.setStyle(Paint.Style.FILL); // Ensure fill style

        if (type == ObjectType.BOMB) {
            canvas.drawCircle(rect.exactCenterX(), rect.exactCenterY(), width / 2f, paint);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(5);
            canvas.drawLine(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10, paint);
            canvas.drawLine(rect.right - 10, rect.top + 10, rect.left + 10, rect.bottom - 10, paint);
        } else if (type == ObjectType.BONUS_POINTS) {
            canvas.drawRect(rect, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("+P", rect.exactCenterX(), rect.exactCenterY()+15, paint);
        } else if (type == ObjectType.BONUS_SLOW) {
            canvas.drawRect(rect, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("S", rect.exactCenterX(), rect.exactCenterY()+15, paint);
        }
        else { // NORMAL
            canvas.drawRect(rect, paint);
        }
    }

    public Rect getRect() {
        return rect;
    }

    public int getColor() {
        return color;
    }

    public ObjectType getType() {
        return type;
    }
}
