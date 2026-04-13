package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class BarChartView extends View {
    private Paint paint;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
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
        // Basic placeholder drawing for the bar chart
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / 15;
        int spacing = width / 30;

        paint.setColor(Color.parseColor("#728AED"));
        for (int i = 0; i < 7; i++) {
            float left = (i + 1) * (barWidth + spacing);
            float top = height - (float)(Math.random() * height * 0.8);
            float right = left + barWidth;
            float bottom = height;
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}