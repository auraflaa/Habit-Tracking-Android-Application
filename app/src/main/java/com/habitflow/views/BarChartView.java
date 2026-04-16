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

import com.habitflow.model.Habit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
        textPaint.setTextSize(spToPx(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Habit> habits) {
        this.habits = habits;
        invalidate();
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
        float paddingBottom = dpToPx(24);
        float chartHeight = height - paddingBottom;
        
        int count = Math.min(habits.size(), 7); 
        float barWidth = width / (float) (count * 2);
        float spacing = (width - (count * barWidth)) / (count + 1);

        // For real stats, we'll check completions over the last 7 days
        List<String> last7Days = getLast7Days();

        for (int i = 0; i < count; i++) {
            Habit h = habits.get(i);
            
            // Calculate real completion ratio for the last 7 days
            int completions = 0;
            for (String date : last7Days) {
                if (h.completedDates.contains(date)) completions++;
            }
            float completionRatio = (float) completions / 7f;
            if (completionRatio < 0.1f) completionRatio = 0.1f; 

            float left = spacing + i * (barWidth + spacing);
            float top = chartHeight - (completionRatio * (chartHeight - dpToPx(20)));
            float right = left + barWidth;
            float bottom = chartHeight;

            try {
                barPaint.setColor(h.colorHex != null ? Color.parseColor(h.colorHex) : accentColor);
            } catch (Exception e) {
                barPaint.setColor(accentColor);
            }
            
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), barPaint);

            canvas.drawText(h.emoji != null ? h.emoji : "•", left + (barWidth / 2), height - dpToPx(6), textPaint);
        }
    }

    private List<String> getLast7Days() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            dates.add(sdf.format(cal.getTime()));
        }
        return dates;
    }

    private void drawPlaceholder(Canvas canvas) {
        textPaint.setTextSize(spToPx(14));
        canvas.drawText("No Habit Data Yet", getWidth() / 2f, getHeight() / 2f, textPaint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
