package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class BarChartView extends View {
    private Paint barPaint;
    private Paint labelPaint;
    private int[] data = new int[0];
    private int maxVal = 1;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(Color.parseColor("#728AED"));
        
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.GRAY);
        labelPaint.setTextSize(22f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int[] newData) {
        this.data = newData;
        this.maxVal = 0;
        for (int val : data) if (val > maxVal) maxVal = val;
        if (maxVal == 0) maxVal = 1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.length == 0) return;

        int w = getWidth();
        int h = getHeight();
        float paddingBottom = 40f;
        float chartHeight = h - paddingBottom;
        
        int count = data.length;
        float spacingRatio = count > 10 ? 0.2f : 0.5f; // Less spacing for more bars
        float availableForBars = w * 0.9f;
        float barWidth = availableForBars / (count + (count + 1) * spacingRatio);
        float spacing = barWidth * spacingRatio;
        float startX = (w - (count * barWidth + (count - 1) * spacing)) / 2f;

        for (int i = 0; i < count; i++) {
            float left = startX + i * (barWidth + spacing);
            float barHeightRatio = (float) data[i] / maxVal;
            float top = chartHeight - (barHeightRatio * (chartHeight * 0.8f));
            
            // Draw bar
            RectF rect = new RectF(left, top, left + barWidth, chartHeight);
            canvas.drawRoundRect(rect, barWidth / 3f, barWidth / 3f, barPaint);
            
            // Labels (Show every label for Week, every 5th for Month)
            if (count <= 7 || i % 5 == 0 || i == count - 1) {
                String label = (i == count - 1) ? "Now" : (count - 1 - i) + "d";
                canvas.drawText(label, left + barWidth/2, h - 10, labelPaint);
            }
        }
    }
}
