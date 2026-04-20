package com.habitflow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.habitflow.activities.MainActivity;
import com.habitflow.adapters.HabitAdapter;
import com.habitflow.data.HabitStore;
import com.habitflow.model.ChecklistItem;
import com.habitflow.model.Habit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private RecyclerView    rvHabits;
    private HabitAdapter    adapter;
    private final List<Habit> displayList = new ArrayList<>();
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
        setDailyQuote();
        refreshData();
    }

    @Override public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (isAdded()) {
            loadHabits(getActiveSegment());
            updateStreakBadge();
        }
    }

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

    private void setupRecyclerView() {
        adapter = new HabitAdapter(displayList, new HabitAdapter.OnHabitClick() {
            @Override public void onCheck(Habit habit, int position) {
                HabitStore.get(requireContext()).toggleComplete(requireContext(), habit.id);
                refreshProgress();
                updateStreakBadge();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).notifyDataChanged();
                }
            }
            @Override public void onLongPress(Habit habit, int position) {
                openEditSheet(habit);
            }
            @Override public void onSubtaskToggle(Habit habit, ChecklistItem item, int position) {
                HabitStore.get(requireContext()).update(requireContext(), habit);
                // No need to refresh progress unless we decide subtasks contribute to overall completion
            }
        });
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHabits.setAdapter(adapter);
        rvHabits.setItemAnimator(null);
    }

    private void setupSegmentChips() {
        chipGroupSegments.setOnCheckedStateChangeListener((group, checkedIds) -> loadHabits(getActiveSegment()));
    }

    private String getActiveSegment() {
        int id = chipGroupSegments.getCheckedChipId();
        if (id == R.id.chip_morning)   return Habit.SEG_MORNING;
        if (id == R.id.chip_afternoon) return Habit.SEG_AFTERNOON;
        if (id == R.id.chip_evening)   return Habit.SEG_EVENING;
        return "ALL";
    }

    private void loadHabits(String segment) {
        displayList.clear();
        List<Habit> source = "ALL".equals(segment)
                ? HabitStore.get(requireContext()).getHabits()
                : HabitStore.get(requireContext()).getBySegment(segment);
        displayList.addAll(source);

        boolean isEmpty = displayList.isEmpty();
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvHabits.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        adapter.notifyDataSetChanged();
        refreshProgress();
    }

    private void refreshProgress() {
        List<Habit> all   = HabitStore.get(requireContext()).getHabits();
        int total         = all.size();
        int done          = HabitStore.get(requireContext()).completedTodayCount();
        int pct           = total > 0 ? (done * 100 / total) : 0;

        tvProgressCount.setText(getString(R.string.habits_complete, done, total));
        pbToday.setProgress(pct);

        if (pct == 0)        tvProgressLabel.setText(R.string.progress_start);
        else if (pct < 50)   tvProgressLabel.setText(getString(R.string.progress_keep_going, pct));
        else if (pct < 100)  tvProgressLabel.setText(getString(R.string.progress_almost_there, pct));
        else                 tvProgressLabel.setText(R.string.progress_done);
    }

    private void updateStreakBadge() {
        int maxStreak = 0;
        for (Habit h : HabitStore.get(requireContext()).getHabits()) {
            if (h.currentStreak > maxStreak) maxStreak = h.currentStreak;
        }
        tvStreak.setText(getResources().getQuantityString(R.plurals.streak_days, maxStreak, maxStreak));
    }

    private void setGreeting() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        String name = prefs.getString("user_name", "User");
        
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        int greetingRes;
        if (hour < 12)      greetingRes = R.string.good_morning;
        else if (hour < 17) greetingRes = R.string.good_afternoon;
        else                greetingRes = R.string.good_evening;
        tvGreeting.setText(greetingRes);
        tvUsername.setText(name + " 👋");
    }

    /** Shows a new quote every day based on the current date. */
    private void setDailyQuote() {
        String[] raw = getResources().getStringArray(R.array.quotes);

        // Use the day of the year as a seed so the quote only changes once every 24 hours
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        // Seeded random based on date
        Random seededRandom = new Random(dayOfYear + year * 365L);
        String entry = raw[seededRandom.nextInt(raw.length)];

        String[] parts = entry.split("\\|");
        tvQuote.setText(getString(R.string.quote_format, parts[0]));
        tvQuoteAuthor.setText(parts.length > 1 ? getString(R.string.quote_author_format, parts[1]) : "");
    }

    private void showRestDayDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("😴 Rest Day")
                .setMessage("Mark today as a rest day? Your streaks won't be broken.")
                .setPositiveButton("Yes, rest today", (d, w) ->
                        Toast.makeText(getContext(), R.string.rest_day_toast,
                                Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openEditSheet(Habit habit) {
        AddHabitSheet sheet = AddHabitSheet.newInstance(habit);
        sheet.setOnSaveListener(this::refreshData);
        sheet.show(getParentFragmentManager(), "edit_habit");
    }
}
