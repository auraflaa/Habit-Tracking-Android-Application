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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Local Storage implementation using SharedPreferences and Gson.
 */
public class HabitStore {

    private static final String PREF_NAME = "habit_flow_prefs";
    private static final String KEY_HABITS = "habits_data";

    private static HabitStore instance;
    private List<Habit> habits = new ArrayList<>();
    private final Gson gson = new Gson();

    private HabitStore(Context context) {
        load(context);
    }

    public static HabitStore get(Context context) {
        if (instance == null) {
            instance = new HabitStore(context.getApplicationContext());
        }
        return instance;
    }

    private void save(Context context) {
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
            if (habits == null) habits = new ArrayList<>();
        }
        syncTodayStatus();
    }

    /** Ensures completedToday flag is accurate based on current system date. */
    public void syncTodayStatus() {
        String todayStr = getTodayStr();
        for (Habit h : habits) {
            h.completedToday = h.completedDates.contains(todayStr);
        }
    }

    private String getTodayStr() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
    }

    public List<Habit> getHabits() { 
        syncTodayStatus();
        return habits; 
    }

    public List<Habit> getBySegment(String segment) {
        syncTodayStatus();
        List<Habit> result = new ArrayList<>();
        for (Habit h : habits) {
            if (segment.equals(h.segment)) result.add(h);
        }
        return result;
    }

    public void add(Context context, Habit h) {
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

    /** Toggle completion for today. */
    public void toggleComplete(Context context, String id) {
        toggleCompleteForDate(context, id, getTodayStr());
    }

    /** Toggle completion for a specific date (yyyy-MM-dd). */
    public void toggleCompleteForDate(Context context, String id, String dateStr) {
        Habit h = findById(id);
        if (h == null) return;
        
        String todayStr = getTodayStr();
        
        if (h.completedDates.contains(dateStr)) {
            h.completedDates.remove(dateStr);
            if (dateStr.equals(todayStr)) h.completedToday = false;
            h.currentStreak = Math.max(0, h.currentStreak - 1);
            h.totalCompletions = Math.max(0, h.totalCompletions - 1);
        } else {
            h.completedDates.add(dateStr);
            if (dateStr.equals(todayStr)) h.completedToday = true;
            h.currentStreak++;
            h.totalCompletions++;
            if (h.currentStreak > h.bestStreak) h.bestStreak = h.currentStreak;
        }
        save(context);
    }

    public int completedTodayCount() {
        syncTodayStatus();
        int c = 0;
        for (Habit h : habits) if (h.completedToday) c++;
        return c;
    }

    public int getCompletedCountForDate(String dateStr) {
        int c = 0;
        for (Habit h : habits) {
            if (h.completedDates.contains(dateStr)) c++;
        }
        return c;
    }
}
