package com.habitflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
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

public class HomeFragment extends Fragment {

    private RecyclerView    rvHabits;
    private HabitAdapter    adapter;
    private List<Habit>     displayList = new ArrayList<>();
    private ProgressBar     pbToday;
    private TextView        tvProgressCount, tvProgressLabel;
    private TextView        tvGreeting, tvUsername, tvStreak;
    private TextView        tvQuote, tvQuoteAuthor;
    private LinearLayout    llEmpty;
    private ChipGroup       chipGroupSegments;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupRecyclerView();
        setupSegmentChips();
        setGreeting();
        setRandomQuote();
        loadHabits("ALL");
        updateStreakBadge();
    }

    @Override public void onResume() {
        super.onResume();
        loadHabits(getActiveSegment());
    }

    // ── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews(View v) {
        rvHabits         = v.findViewById(R.id.rv_habits);
        pbToday          = v.findViewById(R.id.pb_today);
        tvProgressCount  = v.findViewById(R.id.tv_progress_count);
        tvProgressLabel  = v.findViewById(R.id.tv_progress_label);
        tvGreeting       = v.findViewById(R.id.tv_greeting);
        tvUsername       = v.findViewById(R.id.tv_username);
        tvStreak         = v.findViewById(R.id.tv_streak);
        tvQuote          = v.findViewById(R.id.tv_quote);
        tvQuoteAuthor    = v.findViewById(R.id.tv_quote_author);
        llEmpty          = v.findViewById(R.id.ll_empty);
        chipGroupSegments= v.findViewById(R.id.chip_group_segments);

        v.findViewById(R.id.btn_rest_day).setOnClickListener(vv -> showRestDayDialog());
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new HabitAdapter(displayList, new HabitAdapter.OnHabitClick() {
            @Override public void onCheck(Habit habit, int position) {
                HabitStore.get().toggleComplete(habit.id);
                refreshProgress();
            }
            @Override public void onLongPress(Habit habit, int position) {
                openEditSheet(habit);
            }
        });
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHabits.setAdapter(adapter);
        rvHabits.setItemAnimator(null); // disable default flicker
    }

    // ── Segment chips ─────────────────────────────────────────────────────────

    private void setupSegmentChips() {
        chipGroupSegments.setOnCheckedStateChangeListener((group, checkedIds) -> {
            loadHabits(getActiveSegment());
        });
    }

    private String getActiveSegment() {
        int id = chipGroupSegments.getCheckedChipId();
        if (id == R.id.chip_morning)   return Habit.SEG_MORNING;
        if (id == R.id.chip_afternoon) return Habit.SEG_AFTERNOON;
        if (id == R.id.chip_evening)   return Habit.SEG_EVENING;
        return "ALL";
    }

    // ── Load habits ───────────────────────────────────────────────────────────

    private void loadHabits(String segment) {
        displayList.clear();
        List<Habit> source = "ALL".equals(segment)
                ? HabitStore.get().getHabits()
                : HabitStore.get().getBySegment(segment);
        displayList.addAll(source);

        boolean isEmpty = displayList.isEmpty();
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvHabits.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        adapter.notifyDataSetChanged();
        refreshProgress();
    }

    // ── Progress bar ──────────────────────────────────────────────────────────

    private void refreshProgress() {
        List<Habit> all   = HabitStore.get().getHabits();
        int total         = all.size();
        int done          = HabitStore.get().completedTodayCount();
        int pct           = total > 0 ? (done * 100 / total) : 0;

        tvProgressCount.setText(done + " / " + total);
        pbToday.setProgress(pct);

        if (pct == 0)        tvProgressLabel.setText("Add habits to get started!");
        else if (pct < 50)   tvProgressLabel.setText(pct + "% complete — keep going! 💪");
        else if (pct < 100)  tvProgressLabel.setText(pct + "% complete — almost there! 🔥");
        else                 tvProgressLabel.setText("All done for today! 🎉");
    }

    // ── Streak badge ──────────────────────────────────────────────────────────

    private void updateStreakBadge() {
        // Find the best current streak across all habits
        int maxStreak = 0;
        for (Habit h : HabitStore.get().getHabits()) {
            if (h.currentStreak > maxStreak) maxStreak = h.currentStreak;
        }
        tvStreak.setText(maxStreak + " day" + (maxStreak == 1 ? "" : "s"));
    }

    // ── Greeting ──────────────────────────────────────────────────────────────

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12)      greeting = "Good Morning,";
        else if (hour < 17) greeting = "Good Afternoon,";
        else                greeting = "Good Evening,";
        tvGreeting.setText(greeting);
        // TODO: replace "Alex" with logged-in user's name from SharedPreferences
        tvUsername.setText("Alex 👋");
    }

    // ── Random motivational quote ─────────────────────────────────────────────

    private void setRandomQuote() {
        String[] raw = getResources().getStringArray(R.array.quotes);
        String entry = raw[new Random().nextInt(raw.length)];
        String[] parts = entry.split("\\|");
        tvQuote.setText("\"" + parts[0] + "\"");
        tvQuoteAuthor.setText(parts.length > 1 ? "— " + parts[1] : "");
    }

    // ── Rest Day dialog ───────────────────────────────────────────────────────

    private void showRestDayDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("😴 Rest Day")
                .setMessage("Mark today as a rest day? Your streaks won't be broken.")
                .setPositiveButton("Yes, rest today", (d, w) ->
                        Toast.makeText(getContext(), "Rest day marked! Enjoy your break 🌿",
                                Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Edit sheet ────────────────────────────────────────────────────────────

    private void openEditSheet(Habit habit) {
        AddHabitSheet sheet = AddHabitSheet.newInstance(habit);
        sheet.setOnSaveListener(() -> loadHabits(getActiveSegment()));
        sheet.show(getParentFragmentManager(), "edit_habit");
    }
}
