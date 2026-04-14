package com.habitflow.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.habitflow.model.Habit;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Local Storage implementation using SharedPreferences and Gson.
 * This can be easily swapped for a Room database or a Remote API later.
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

    // ── Persistence ──────────────────────────────────────────────────────────

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
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public List<Habit> getHabits() { return habits; }

    public List<Habit> getBySegment(String segment) {
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

    /** Toggle completedToday flag and update streak. */
    public void toggleComplete(Context context, String id) {
        Habit h = findById(id);
        if (h == null) return;
        h.completedToday = !h.completedToday;
        if (h.completedToday) {
            h.currentStreak++;
            h.totalCompletions++;
            if (h.currentStreak > h.bestStreak) h.bestStreak = h.currentStreak;
        } else {
            h.currentStreak = Math.max(0, h.currentStreak - 1);
            h.totalCompletions = Math.max(0, h.totalCompletions - 1);
        }
        save(context);
    }

    /** Count how many habits are completed today. */
    public int completedTodayCount() {
        int c = 0;
        for (Habit h : habits) if (h.completedToday) c++;
        return c;
    }
}
