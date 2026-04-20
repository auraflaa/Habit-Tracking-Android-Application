package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HeatmapView extends View {
    private Paint paint;
    private float cornerRadius;
    private List<Habit> habits;

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

    public void setData(List<Habit> habits) {
        this.habits = habits;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int columns = 20; // Show approx 20 weeks of data
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
        
        // Get theme-aware empty color
        int colorEmpty = 0x1A1A24;
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(com.habitflow.R.attr.customElevatedBackground, typedValue, true)) {
            colorEmpty = typedValue.data;
        }

        // Material Green levels
        int[] levels = {
            colorEmpty,                  // Empty
            Color.parseColor("#1A4D1A"), // Low
            Color.parseColor("#2D7D2D"), // Medium-Low
            Color.parseColor("#43B043"), // Medium-High
            Color.parseColor("#7AD326")  // High (Completed all)
        };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        
        // Go back (columns * 7) days to start the grid
        cal.add(Calendar.DAY_OF_YEAR, -(columns * rows) + 1);
        
        // Snap to Sunday if we want consistent rows
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                String dateKey = sdf.format(cal.getTime());
                
                int level = 0;
                if (habits != null && !habits.isEmpty()) {
                    int completed = 0;
                    for (Habit h : habits) {
                        if (h.completedDates.contains(dateKey)) completed++;
                    }
                    
                    float pct = (float) completed / habits.size();
                    if (pct > 0.99f) level = 4;
                    else if (pct > 0.66f) level = 3;
                    else if (pct > 0.33f) level = 2;
                    else if (pct > 0.01f) level = 1;
                }

                float left = i * (size + spacing);
                float top = j * (size + spacing);
                float right = left + size;
                float bottom = top + size;
                
                paint.setColor(levels[level]);
                
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
                
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}