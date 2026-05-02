package com.habitflow.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.habitflow.R;
import com.habitflow.activities.MainActivity;
import com.habitflow.adapters.HabitAdapter;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private GridLayout      gridCalendar;
    private TextView        tvMonth, tvSelectedDate, tvSelectedRate;
    private RecyclerView    rvDayHabits;
    private HabitAdapter    dayAdapter;
    private final List<Habit> dayHabits = new ArrayList<>();

    private Calendar currentCal;
    private int selectedDay = -1;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridCalendar    = view.findViewById(R.id.grid_calendar);
        tvMonth         = view.findViewById(R.id.tv_month);
        tvSelectedDate  = view.findViewById(R.id.tv_selected_date);
        tvSelectedRate  = view.findViewById(R.id.tv_selected_rate);
        rvDayHabits     = view.findViewById(R.id.rv_day_habits);

        currentCal = Calendar.getInstance();
        selectedDay = currentCal.get(Calendar.DAY_OF_MONTH);

        setupDayRecycler();
        setupNavButtons(view);
        
        // Use post to ensure view is measured before rendering
        gridCalendar.post(this::renderCalendar);
        showDayHabits(selectedDay);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void onHabitAdded() {
        if (isAdded()) {
            refreshData();
        }
    }

    private void refreshData() {
        if (selectedDay != -1) {
            showDayHabits(selectedDay);
        }
        renderCalendar();
    }

    private void setupDayRecycler() {
        dayAdapter = new HabitAdapter(dayHabits, new HabitAdapter.OnHabitClick() {
            @Override
            public void onCheck(Habit habit, int position) {
                if (selectedDay == -1) return;

                Calendar target = (Calendar) currentCal.clone();
                target.set(Calendar.DAY_OF_MONTH, selectedDay);
                String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(target.getTime());

                HabitStore.get(requireContext()).toggleCompleteForDate(requireContext(), habit.id, dateStr);
                
                refreshData();
                
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).notifyDataChanged();
                }
            }

            @Override
            public void onLongPress(Habit habit, int position) {
                // Optional: Open edit sheet
            }
        });
        rvDayHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDayHabits.setAdapter(dayAdapter);
    }

    private void setupNavButtons(View v) {
        ImageButton btnPrev = v.findViewById(R.id.btn_prev);
        ImageButton btnNext = v.findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(vv -> {
            currentCal.add(Calendar.MONTH, -1);
            selectedDay = -1;
            renderCalendar();
        });
        btnNext.setOnClickListener(vv -> {
            currentCal.add(Calendar.MONTH, 1);
            selectedDay = -1;
            renderCalendar();
        });
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private void renderCalendar() {
        if (!isAdded()) return;
        gridCalendar.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(sdf.format(currentCal.getTime()));

        Calendar cal = (Calendar) currentCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1; 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();

        // Colors from theme
        int colorPrimary = Color.parseColor("#728AED");
        int colorTextPrimary = getThemeColor(R.attr.customTextPrimary);
        int colorTextSecondary = getThemeColor(R.attr.customTextSecondary);
        int colorEmptyCell = getThemeColor(R.attr.customCardBackground);

        // Calculate cell width based on actual grid width minus horizontal padding
        int totalPadding = gridCalendar.getPaddingLeft() + gridCalendar.getPaddingRight();
        int availableWidth = gridCalendar.getWidth() > 0 ? gridCalendar.getWidth() : getResources().getDisplayMetrics().widthPixels;
        int cellWidth  = (availableWidth - totalPadding) / 7;
        int cellHeight = (int)(cellWidth * 1.05f);

        for (int i = 0; i < firstDow; i++) addBlankCell(cellWidth, cellHeight);

        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;

            boolean isToday = (today.get(Calendar.YEAR)  == currentCal.get(Calendar.YEAR) &&
                               today.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH) &&
                               today.get(Calendar.DAY_OF_MONTH) == day);

            boolean isSelected = (d == selectedDay);

            float completionPct = getCompletionForDay(day);

            TextView cell = new TextView(requireContext());
            cell.setText(String.valueOf(day));
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(13f);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width  = cellWidth;
            lp.height = cellHeight;
            cell.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);

            if (isToday) {
                bg.setColor(colorPrimary);
                cell.setTextColor(Color.WHITE);
                cell.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (isSelected) {
                bg.setColor(Color.TRANSPARENT);
                bg.setStroke(dpToPx(2), colorPrimary);
                cell.setTextColor(colorPrimary);
            } else {
                int cellBgColor = completionPct < 0.01f ? colorEmptyCell : heatmapColor(completionPct);
                bg.setColor(cellBgColor);
                // Contrast logic: If background is dark (heatmap), use White text. 
                // Otherwise use the standard secondary text color.
                if (completionPct < 0.01f) {
                    cell.setTextColor(colorTextSecondary);
                } else {
                    cell.setTextColor(Color.WHITE);
                }
            }

            cell.setBackground(bg);
            int padding = dpToPx(4);
            cell.setPadding(padding, padding, padding, padding);
            cell.setOnClickListener(v -> {
                selectedDay = d;
                renderCalendar();
                showDayHabits(d);
            });

            gridCalendar.addView(cell);
        }
    }

    private void addBlankCell(int w, int h) {
        View blank = new View(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = w; lp.height = h;
        blank.setLayoutParams(lp);
        gridCalendar.addView(blank);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showDayHabits(int day) {
        if (!isAdded()) return;
        Calendar target = (Calendar) currentCal.clone();
        target.set(Calendar.DAY_OF_MONTH, day);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(target.getTime());

        SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        boolean isToday = (today.get(Calendar.YEAR)  == target.get(Calendar.YEAR) &&
                           today.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
                           today.get(Calendar.DAY_OF_MONTH) == day);
        tvSelectedDate.setText(isToday ? "Today" : fmt.format(target.getTime()));

        dayHabits.clear();
        for (Habit h : HabitStore.get(requireContext()).getHabits()) {
            Habit display = new Habit();
            display.id = h.id;
            display.name = h.name;
            display.emoji = h.emoji;
            display.colorHex = h.colorHex;
            display.category = h.category;
            display.priority = h.priority;
            display.currentStreak = h.currentStreak;
            display.completedToday = h.completedDates.contains(dateStr);
            dayHabits.add(display);
        }
        dayAdapter.notifyDataSetChanged();

        float pct = getCompletionForDay(day);
        int pctInt = Math.round(pct * 100);
        tvSelectedRate.setText(getString(R.string.completion_percentage, pctInt));
    }

    private float getCompletionForDay(int day) {
        Calendar cal = (Calendar) currentCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        
        HabitStore store = HabitStore.get(requireContext());
        List<Habit> allHabits = store.getHabits();
        if (allHabits.isEmpty()) return 0f;
        
        int completedCount = store.getCompletedCountForDate(dateStr);
        return (float) completedCount / allHabits.size();
    }

    private int heatmapColor(float pct) {
        // These are brand colors, they can remain consistent or be slightly adjusted
        if (pct < 0.35f) return Color.parseColor("#2A3A1A");
        if (pct < 0.65f) return Color.parseColor("#4A6A2A");
        return Color.parseColor("#7AD326");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
