package com.habitflow.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.habitflow.model.Habit;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Local Storage implementation using SharedPreferences and Gson.
 */
public class HabitStore {

    private static final String PREF_NAME = "habit_flow_prefs";
    private static final String KEY_HABITS = "habits_data";
    private static final String KEY_LAST_DATE = "last_reset_date";

    private static HabitStore instance;
    private List<Habit> habits = new ArrayList<>();
    private final Gson gson = new Gson();

    private HabitStore(Context context) {
        load(context);
        checkNewDay(context);
    }

    public static HabitStore get(Context context) {
        if (instance == null) {
            instance = new HabitStore(context.getApplicationContext());
        }
        return instance;
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(habits);
        prefs.edit().putString(KEY_HABITS, json).apply();
    }

    private void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HABITS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
            habits = gson.fromJson(json, type);
            if (habits == null) {
                habits = new ArrayList<>();
            } else {
                for (Habit h : habits) {
                    h.ensureInitialized();
                }
            }
        }
        syncTodayStatus();
    }

    private void checkNewDay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastDateStr = prefs.getString(KEY_LAST_DATE, "");
        String todayStr = getTodayString();

        if (!todayStr.equals(lastDateStr)) {
            // Check if we skipped yesterday (to reset streaks)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

            for (Habit h : habits) {
                // Streaks only apply to Habits, not Tasks
                if (Habit.TYPE_HABIT.equals(h.type)) {
                    // If yesterday was NOT completed AND NOT a rest day, streak resets
                    boolean finishedYesterday = h.completedDates.contains(yesterdayStr);
                    boolean wasRestDay = h.restDates.contains(yesterdayStr);

                    if (!finishedYesterday && !wasRestDay) {
                        h.currentStreak = 0;
                    }
                }

                h.completedToday = false;
            }
            
            save(context);
            prefs.edit().putString(KEY_LAST_DATE, todayStr).apply();
        }
    }

    /** Ensures completedToday flag is accurate based on current system date. */
    public void syncTodayStatus() {
        String todayStr = getTodayString();
        for (Habit h : habits) {
            h.completedToday = h.completedDates.contains(todayStr);
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public List<Habit> getHabits() { 
        syncTodayStatus();
        return habits; 
    }

    public void add(Context context, Habit h) {
        h.ensureInitialized();
        habits.add(h);
        save(context);
    }

    public void update(Context context, Habit updated) {
        for (int i = 0; i < habits.size(); i++) {
            if (Objects.equals(habits.get(i).id, updated.id)) {
                habits.set(i, updated);
                save(context);
                return;
            }
        }
    }

    public void delete(Context context, String id) {
        habits.removeIf(h -> Objects.equals(h.id, id));
        save(context);
    }

    public Habit findById(String id) {
        for (Habit h : habits) {
            if (Objects.equals(h.id, id)) return h;
        }
        return null;
    }

    public void toggleComplete(Context context, String id) {
        Habit h = findById(id);
        if (h == null) return;
        
        String todayStr = getTodayString();
        // Use the unified logic
        toggleCompleteForDate(context, id, todayStr);
    }

    /** Toggle completion for a specific date (yyyy-MM-dd). */
    public void toggleCompleteForDate(Context context, String id, String dateStr) {
        Habit h = findById(id);
        if (h == null) return;
        
        String todayStr = getTodayString();
        // Toggle the state
        if (h.completedDates.contains(dateStr)) {
            h.completedDates.remove(dateStr);
            if (dateStr.equals(todayStr)) h.completedToday = false;
            if (Habit.TYPE_HABIT.equals(h.type)) {
                h.currentStreak = Math.max(0, h.currentStreak - 1);
            }
            h.totalCompletions = Math.max(0, h.totalCompletions - 1);
        } else {
            h.completedDates.add(dateStr);
            h.restDates.remove(dateStr); // Completion overrides rest
            if (dateStr.equals(todayStr)) h.completedToday = true;
            if (Habit.TYPE_HABIT.equals(h.type)) {
                h.currentStreak++;
                if (h.currentStreak > h.bestStreak) h.bestStreak = h.currentStreak;
            }
            h.totalCompletions++;
        }
        save(context);
    }

    public void markRestDay(Context context) {
        String today = getTodayString();
        for (Habit h : habits) {
            if (!h.completedDates.contains(today)) {
                h.restDates.add(today);
            }
        }
        save(context);
    }

    public int completedTodayCount() {
        syncTodayStatus();
        int c = 0;
        for (Habit h : habits) {
            if (h.completedToday) c++;
        }
        return c;
    }

    public int getCompletedCountForDate(String dateStr) {
        int c = 0;
        for (Habit h : habits) {
            if (h.completedDates.contains(dateStr)) c++;
        }
        return c;
    }

    private String getTodayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
