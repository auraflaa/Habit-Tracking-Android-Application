package com.habitflow.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
        renderCalendar();
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
        dayAdapter = new HabitAdapter(dayHabits, null);
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

    private void renderCalendar() {
        gridCalendar.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(sdf.format(currentCal.getTime()));

        Calendar cal = (Calendar) currentCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Sunday is 1, Monday is 2... so firstDow is 0 for Sunday, 1 for Monday, etc.
        // This matches our S M T W T F S headers in XML.
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY; 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Use weights for width distribution to ensure 7 columns always fit perfectly.
        // We calculate height based on the theoretical width to keep cells proportional.
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int horizontalOffset = dpToPx(80); // Card margin (24*2) + Padding (16*2)
        int cellHeight = (int) (((screenWidth - horizontalOffset) / 7f) * 0.9f);

        for (int i = 0; i < firstDow; i++) addBlankCell(cellHeight);

        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;
            
            Calendar cellDate = (Calendar) currentCal.clone();
            cellDate.set(Calendar.DAY_OF_MONTH, day);
            cellDate.set(Calendar.HOUR_OF_DAY, 0);
            cellDate.set(Calendar.MINUTE, 0);
            cellDate.set(Calendar.SECOND, 0);
            cellDate.set(Calendar.MILLISECOND, 0);

            boolean isToday = cellDate.equals(today);
            boolean isPast = cellDate.before(today);
            boolean isSelected = (d == selectedDay);

            float completionPct = calculateDayCompletion(cellDate);

            TextView cell = new TextView(requireContext());
            cell.setText(String.valueOf(day));
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(14f);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            lp.width = 0;
            lp.height = cellHeight;
            
            int marginV = dpToPx(1);
            int marginH = dpToPx(1); 
            lp.setMargins(marginH, marginV, marginH, marginV);
            cell.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            
            if (isPast) {
                bg.setCornerRadius(0);
                bg.setColor(Color.parseColor("#14141F"));
                cell.setTextColor(Color.parseColor("#5A5A75"));
            } else if (isSelected) {
                bg.setCornerRadius(dpToPx(12));
                bg.setColor(Color.parseColor("#1A728AED"));
                bg.setStroke(dpToPx(1), Color.parseColor("#728AED"));
                cell.setTextColor(Color.parseColor("#728AED"));
                cell.setTypeface(null, Typeface.BOLD);
            } else {
                bg.setCornerRadius(dpToPx(12));
                bg.setColor(heatmapColor(completionPct));
                if (isToday) {
                    cell.setTextColor(Color.parseColor("#728AED"));
                    cell.setTypeface(null, Typeface.BOLD);
                } else {
                    cell.setTextColor(Color.parseColor("#EEEAE0"));
                    cell.setTypeface(null, Typeface.NORMAL);
                }
            }

            cell.setBackground(bg);
            cell.setOnClickListener(v -> {
                selectedDay = d;
                renderCalendar();
                showDayHabits(d);
            });

            gridCalendar.addView(cell);
        }
    }

    private void addBlankCell(int h) {
        View blank = new View(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
        );
        lp.width = 0;
        lp.height = h;
        int marginH = dpToPx(1);
        lp.setMargins(marginH, dpToPx(1), marginH, dpToPx(1));
        blank.setLayoutParams(lp);
        gridCalendar.addView(blank);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showDayHabits(int day) {
        Calendar target = (Calendar) currentCal.clone();
        target.set(Calendar.DAY_OF_MONTH, day);
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(target.getTime());

        SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        boolean isToday = (today.get(Calendar.YEAR)  == target.get(Calendar.YEAR) &&
                           today.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
                           today.get(Calendar.DAY_OF_MONTH) == day);
        tvSelectedDate.setText(isToday ? "Today" : fmt.format(target.getTime()));

        dayHabits.clear();
        List<Habit> all = HabitStore.get(requireContext()).getHabits();

        for (Habit h : all) {
            if (h.completedDates.contains(dateKey)) {
                dayHabits.add(h);
            }
        }

        dayAdapter.notifyDataSetChanged();

        float pct = calculateDayCompletion(target);
        int pctInt = Math.round(pct * 100);
        tvSelectedRate.setText(getString(R.string.completion_percentage, pctInt));
    }

    private float calculateDayCompletion(Calendar date) {
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        List<Habit> all = HabitStore.get(requireContext()).getHabits();
        if (all.isEmpty()) return 0;

        int completed = 0;
        for (Habit h : all) {
            if (h.completedDates.contains(dateKey)) completed++;
        }
        return (float) completed / all.size();
    }

    private int heatmapColor(float pct) {
        if (pct < 0.01f) return Color.parseColor("#12121A");
        if (pct < 0.35f) return Color.parseColor("#1A1A2E");
        if (pct < 0.65f) return Color.parseColor("#2A2A4E");
        return Color.parseColor("#4A4A8E");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
