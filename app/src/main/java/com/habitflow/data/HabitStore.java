package com.habitflow.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.habitflow.data.db.HabitDao;
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
 * Local Storage implementation using SQLite (via HabitDao) with migration from SharedPreferences.
 */
public class HabitStore {

    private static final String PREF_NAME = "habit_flow_prefs";
    private static final String KEY_HABITS = "habits_data";
    private static final String KEY_LAST_DATE = "last_reset_date";
    private static final String KEY_MIGRATED_TO_DB = "migrated_to_db";

    private static HabitStore instance;
    private final HabitDao dao;
    private List<Habit> cachedHabits = new ArrayList<>();

    private HabitStore(Context context) {
        this.dao = new HabitDao(context);
        migrateIfNeeded(context);
        refreshCache();
        checkNewDay(context);
    }

    public static HabitStore get(Context context) {
        if (instance == null) {
            instance = new HabitStore(context.getApplicationContext());
        }
        return instance;
    }

    private void refreshCache() {
        cachedHabits = dao.getAllHabits();
        syncTodayStatus();
    }

    // ── Migration ──────────────────────────────────────────────────────────

    private void migrateIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_MIGRATED_TO_DB, false)) return;

        String json = prefs.getString(KEY_HABITS, null);
        if (json != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
                List<Habit> oldHabits = gson.fromJson(json, type);
                if (oldHabits != null) {
                    for (Habit h : oldHabits) {
                        h.ensureInitialized();
                        dao.insert(h);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putBoolean(KEY_MIGRATED_TO_DB, true).apply();
    }

    // ── Day Management ───────────────────────────────────────────────────────

    private void checkNewDay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastDateStr = prefs.getString(KEY_LAST_DATE, "");
        String todayStr = getTodayString();

        if (!todayStr.equals(lastDateStr)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

            boolean changed = false;
            for (Habit h : cachedHabits) {
                if (Habit.TYPE_HABIT.equals(h.type)) {
                    boolean finishedYesterday = h.completedDates.contains(yesterdayStr);
                    boolean wasRestDay = h.restDates.contains(yesterdayStr);

                    if (!finishedYesterday && !wasRestDay) {
                        h.currentStreak = 0;
                        changed = true;
                    }
                }
                h.completedToday = false;
                if (changed) dao.update(h);
            }
            
            if (changed) refreshCache();
            prefs.edit().putString(KEY_LAST_DATE, todayStr).apply();
        }
    }

    public void syncTodayStatus() {
        String todayStr = getTodayString();
        for (Habit h : cachedHabits) {
            h.completedToday = h.completedDates.contains(todayStr);
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public List<Habit> getHabits() { 
        syncTodayStatus();
        return cachedHabits; 
    }

    public void add(Context context, Habit h) {
        h.ensureInitialized();
        dao.insert(h);
        refreshCache();
    }

    public void update(Context context, Habit updated) {
        dao.update(updated);
        refreshCache();
    }

    public void delete(Context context, String id) {
        dao.delete(id);
        refreshCache();
    }

    public Habit findById(String id) {
        for (Habit h : cachedHabits) {
            if (Objects.equals(h.id, id)) return h;
        }
        return null;
    }

    public void toggleComplete(Context context, String id) {
        toggleCompleteForDate(context, id, getTodayString());
    }

    public void toggleCompleteForDate(Context context, String id, String dateStr) {
        Habit h = findById(id);
        if (h == null) return;
        
        String todayStr = getTodayString();
        if (h.completedDates.contains(dateStr)) {
            h.completedDates.remove(dateStr);
            if (dateStr.equals(todayStr)) h.completedToday = false;
            if (Habit.TYPE_HABIT.equals(h.type)) {
                h.currentStreak = Math.max(0, h.currentStreak - 1);
            }
            h.totalCompletions = Math.max(0, h.totalCompletions - 1);
        } else {
            h.completedDates.add(dateStr);
            h.restDates.remove(dateStr); 
            if (dateStr.equals(todayStr)) h.completedToday = true;
            if (Habit.TYPE_HABIT.equals(h.type)) {
                h.currentStreak++;
                if (h.currentStreak > h.bestStreak) h.bestStreak = h.currentStreak;
            }
            h.totalCompletions++;
        }
        dao.update(h);
        refreshCache();
    }

    public void markRestDay(Context context) {
        String today = getTodayString();
        for (Habit h : cachedHabits) {
            if (!h.completedDates.contains(today)) {
                h.restDates.add(today);
                dao.update(h);
            }
        }
        refreshCache();
    }

    public int completedTodayCount() {
        int c = 0;
        for (Habit h : cachedHabits) {
            if (h.completedToday) c++;
        }
        return c;
    }

    public int getCompletedCountForDate(String dateStr) {
        int c = 0;
        for (Habit h : cachedHabits) {
            if (h.completedDates.contains(dateStr)) c++;
        }
        return c;
    }

    private String getTodayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
