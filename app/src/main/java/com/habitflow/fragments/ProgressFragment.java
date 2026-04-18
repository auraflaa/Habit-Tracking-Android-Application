package com.habitflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.habitflow.R;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;
import com.habitflow.views.BarChartView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private TextView    tvBestStreak, tvTotal, tvCurrentStreak;
    private RecyclerView rvBreakdown;
    private TabLayout   tabScope;
    private BarChartView barChart;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBestStreak    = view.findViewById(R.id.tv_best_streak);
        tvTotal         = view.findViewById(R.id.tv_total);
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak);
        rvBreakdown     = view.findViewById(R.id.rv_breakdown);
        tabScope        = view.findViewById(R.id.tab_scope);
        barChart        = view.findViewById(R.id.bar_chart);

        refreshData();
        setupTabs();
    }

    @Override public void onResume() { 
        super.onResume(); 
        refreshData();
    }

    public void onHabitAdded() {
        if (isAdded()) {
            refreshData();
        }
    }

    public void refreshData() {
        if (isAdded()) {
            loadStats();
            setupBreakdown();
            updateChart();
        }
    }

    private void loadStats() {
        List<Habit> habits = HabitStore.get(requireContext()).getHabits();
        int bestStreak = 0, currentStreak = 0, total = 0;
        for (Habit h : habits) {
            if (h.bestStreak    > bestStreak)    bestStreak    = h.bestStreak;
            if (h.currentStreak > currentStreak) currentStreak = h.currentStreak;
            total += h.totalCompletions;
        }
        tvBestStreak.setText(String.valueOf(bestStreak));
        tvCurrentStreak.setText(String.valueOf(currentStreak));
        tvTotal.setText(String.valueOf(total));
    }

    private void updateChart() {
        int daysToLoad = 7;
        int selectedTab = tabScope.getSelectedTabPosition();
        if (selectedTab == 1) daysToLoad = 7;   // Week
        else if (selectedTab == 2) daysToLoad = 30; // Month
        else daysToLoad = 7; // Default to Week for "Day" tab too (or you can customize it)

        int[] data = new int[daysToLoad];
        HabitStore store = HabitStore.get(requireContext());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(daysToLoad - 1));

        for (int i = 0; i < daysToLoad; i++) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
            data[i] = store.getCompletedCountForDate(dateStr);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        barChart.setData(data);
    }

    private void setupBreakdown() {
        List<Habit> habits = HabitStore.get(requireContext()).getHabits();
        BreakdownAdapter adapter = new BreakdownAdapter(habits);
        rvBreakdown.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBreakdown.setAdapter(adapter);
        rvBreakdown.setNestedScrollingEnabled(false);
    }

    private void setupTabs() {
        tabScope.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                updateChart();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    static class BreakdownAdapter extends RecyclerView.Adapter<BreakdownAdapter.VH> {
        private final List<Habit> items;
        BreakdownAdapter(List<Habit> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_habit_progress, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Habit habit = items.get(position);
            h.tvEmoji.setText(habit.emoji);
            h.tvName.setText(habit.name);
            h.tvStreak.setText("🔥 " + habit.currentStreak + " streak");
            h.tvTotal.setText("✅ " + habit.totalCompletions + " total");

            int pct = Math.min(100, habit.totalCompletions * 5);
            h.pbHabit.setProgress(pct);
            h.tvPct.setText(pct + "%");
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvStreak, tvTotal, tvPct;
            ProgressBar pbHabit;
            VH(View v) {
                super(v);
                tvEmoji  = v.findViewById(R.id.tv_emoji);
                tvName   = v.findViewById(R.id.tv_name);
                tvStreak = v.findViewById(R.id.tv_streak);
                tvTotal  = v.findViewById(R.id.tv_total);
                tvPct    = v.findViewById(R.id.tv_pct);
                pbHabit  = v.findViewById(R.id.pb_habit);
            }
        }
    }
}
