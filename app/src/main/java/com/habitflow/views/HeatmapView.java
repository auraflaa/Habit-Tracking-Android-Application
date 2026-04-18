package com.habitflow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;

import com.habitflow.R;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HeatmapView extends View {
    private Paint paint;
    private float cornerRadius;
    private final Map<String, Integer> completionData = new HashMap<>();
    private int totalHabitsCount = 0;

    public HeatmapView(Context context) {
        super(context);
        init();
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerRadius = dpToPx(2);
        loadRealData();
    }

    public void loadRealData() {
        completionData.clear();
        HabitStore store = HabitStore.get(getContext());
        List<Habit> allHabits = store.getHabits();
        totalHabitsCount = allHabits.size();

        if (totalHabitsCount == 0) return;

        // Fetch data for the last 140 days (20 weeks)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -139);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (int i = 0; i < 140; i++) {
            String dateStr = sdf.format(cal.getTime());
            int count = store.getCompletedCountForDate(dateStr);
            completionData.put(dateStr, count);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        invalidate();
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int columns = 20;
        int rows = 7;
        int size = dpToPx(10);
        int spacing = dpToPx(3);
        
        int width = (columns * (size + spacing)) - spacing;
        int height = (rows * (size + spacing)) - spacing;
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int columns = 20;
        int rows = 7;
        int size = dpToPx(10);
        int spacing = dpToPx(3);
        
        // Resolve the empty cell color from the theme
        int colorEmpty = getThemeColor(R.attr.customCardBackground);

        int[] levels = {
            colorEmpty,    // Level 0 (Empty) - Theme aware
            0x447AD326,    // Level 1
            0x887AD326,    // Level 2
            0xCC7AD326,    // Level 3
            0xFF7AD326     // Level 4
        };

        Calendar cal = Calendar.getInstance();
        // Move back to the start of the 20-week period (Sunday of that week)
        cal.add(Calendar.WEEK_OF_YEAR, -19);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                String dateStr = sdf.format(cal.getTime());
                int completedCount = completionData.getOrDefault(dateStr, 0);
                
                int colorIndex = 0;
                if (totalHabitsCount > 0 && completedCount > 0) {
                    float pct = (float) completedCount / totalHabitsCount;
                    if (pct <= 0.25f) colorIndex = 1;
                    else if (pct <= 0.50f) colorIndex = 2;
                    else if (pct <= 0.75f) colorIndex = 3;
                    else colorIndex = 4;
                }

                paint.setColor(levels[colorIndex]);

                float left = i * (size + spacing);
                float top = j * (size + spacing);
                
                RectF rect = new RectF(left, top, left + size, top + size);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
                
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
