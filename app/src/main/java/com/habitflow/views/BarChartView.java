package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import com.habitflow.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private Paint barPaint;
    private Paint textPaint;
    private List<Habit> habits = new ArrayList<>();
    private int accentColor = Color.parseColor("#728AED");

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Habit> habits) {
        this.habits = habits;
        invalidate(); // Redraw with real data
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (habits == null || habits.isEmpty()) {
            drawPlaceholder(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 40;
        int chartHeight = height - paddingBottom;
        
        int count = Math.min(habits.size(), 7); // Show up to 7 habits
        float barWidth = width / (float) (count * 2);
        float spacing = (width - (count * barWidth)) / (count + 1);

        for (int i = 0; i < count; i++) {
            Habit h = habits.get(i);
            
            // Calculate completion percentage (mock logic since we don't have history yet)
            // Using totalCompletions / (max of 30 days) as a proxy
            float completionRatio = Math.min(1.0f, h.totalCompletions / 30f);
            if (completionRatio < 0.1f) completionRatio = 0.1f; // Minimum height for visibility

            float left = spacing + i * (barWidth + spacing);
            float top = chartHeight - (completionRatio * (chartHeight - 40));
            float right = left + barWidth;
            float bottom = chartHeight;

            // Draw bar
            try {
                barPaint.setColor(h.colorHex != null ? Color.parseColor(h.colorHex) : accentColor);
            } catch (Exception e) {
                barPaint.setColor(accentColor);
            }
            
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 12f, 12f, barPaint);

            // Draw label (emoji)
            canvas.drawText(h.emoji != null ? h.emoji : "•", left + (barWidth / 2), height - 10, textPaint);
        }
    }

    private void drawPlaceholder(Canvas canvas) {
        // Simple placeholder if no data
        textPaint.setTextSize(32f);
        canvas.drawText("No Habit Data Yet", getWidth() / 2f, getHeight() / 2f, textPaint);
    }
}
