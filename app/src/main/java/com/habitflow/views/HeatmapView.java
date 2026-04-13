package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class HeatmapView extends View {
    private Paint paint;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Placeholder for heatmap (grid of squares)
        int size = 40;
        int spacing = 10;
        
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 5; j++) {
                float left = i * (size + spacing);
                float top = j * (size + spacing);
                float right = left + size;
                float bottom = top + size;
                
                // Random green shades
                int alpha = (int) (Math.random() * 200) + 55;
                paint.setColor(Color.argb(alpha, 122, 211, 38));
                
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}