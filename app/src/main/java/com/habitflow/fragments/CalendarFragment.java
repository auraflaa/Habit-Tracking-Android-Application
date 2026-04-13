package com.habitflow.fragments;

import android.graphics.Color;
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
import java.util.Random;

public class CalendarFragment extends Fragment {

    private GridLayout      gridCalendar;
    private TextView        tvMonth, tvSelectedDate, tvSelectedRate;
    private RecyclerView    rvDayHabits;
    private HabitAdapter    dayAdapter;
    private List<Habit>     dayHabits = new ArrayList<>();

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
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1; 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();

        int cellWidth  = getResources().getDisplayMetrics().widthPixels / 7;
        int cellHeight = (int)(cellWidth * 1.05f);

        for (int i = 0; i < firstDow; i++) addBlankCell(cellWidth, cellHeight);

        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;

            boolean isToday = (today.get(Calendar.YEAR)  == currentCal.get(Calendar.YEAR) &&
                               today.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH) &&
                               today.get(Calendar.DAY_OF_MONTH) == day);

            boolean isSelected = (d == selectedDay);

            float completionPct = fakeCompletion(day);

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
                bg.setColor(Color.parseColor("#728AED"));
                cell.setTextColor(Color.WHITE);
                cell.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (isSelected) {
                bg.setColor(Color.TRANSPARENT);
                bg.setStroke(dpToPx(2), Color.parseColor("#728AED"));
                cell.setTextColor(Color.parseColor("#728AED"));
            } else {
                bg.setColor(heatmapColor(completionPct));
                cell.setTextColor(completionPct > 0.5f
                    ? Color.parseColor("#EEEAE0")
                    : Color.parseColor("#8A8880"));
            }

            cell.setBackground(bg);
            cell.setPadding(4, 4, 4, 4);
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

    private void showDayHabits(int day) {
        Calendar target = (Calendar) currentCal.clone();
        target.set(Calendar.DAY_OF_MONTH, day);

        SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        boolean isToday = (today.get(Calendar.YEAR)  == target.get(Calendar.YEAR) &&
                           today.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
                           today.get(Calendar.DAY_OF_MONTH) == day);
        tvSelectedDate.setText(isToday ? "Today" : fmt.format(target.getTime()));

        dayHabits.clear();
        dayHabits.addAll(HabitStore.get().getHabits());
        dayAdapter.notifyDataSetChanged();

        float pct = fakeCompletion(day);
        int pctInt = Math.round(pct * 100);
        tvSelectedRate.setText(pctInt + "% completion");
    }

    private float fakeCompletion(int day) {
        Random rng = new Random(day * 31L + currentCal.get(Calendar.MONTH) * 7);
        return rng.nextFloat();
    }

    private int heatmapColor(float pct) {
        if (pct < 0.01f) return Color.parseColor("#21212E");
        if (pct < 0.35f) return Color.parseColor("#2A3A1A");
        if (pct < 0.65f) return Color.parseColor("#4A6A2A");
        return Color.parseColor("#7AD326");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
