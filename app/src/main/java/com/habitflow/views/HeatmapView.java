package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.habitflow.R;

public class HeatmapView extends View {
    private Paint paint;
    private float cornerRadius;

    public HeatmapView(Context context) {
        super(context);
        init();
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        cornerRadius = dpToPx(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate required width for 20 columns
        int columns = 20;
        int rows = 7;
        int size = dpToPx(12);
        int spacing = dpToPx(4);
        
        int width = (columns * (size + spacing)) + getPaddingLeft() + getPaddingRight();
        int height = (rows * (size + spacing)) + getPaddingTop() + getPaddingBottom();
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int columns = 20;
        int rows = 7;
        int size = dpToPx(12);
        int spacing = dpToPx(4);
        
        // Colors from resources
        int[] levels = {
            0xFF1A1A24, // Level 0 (Empty)
            0x447AD326, // Level 1
            0x887AD326, // Level 2
            0xCC7AD326, // Level 3
            0xFF7AD326  // Level 4
        };

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                float left = i * (size + spacing);
                float top = j * (size + spacing);
                float right = left + size;
                float bottom = top + size;
                
                // Simulate some data
                int level = (int) (Math.random() * 5);
                paint.setColor(levels[level]);
                
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}