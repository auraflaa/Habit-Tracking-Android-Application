package com.habitflow.data;

import com.habitflow.model.Habit;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory store for demo/UI purposes.
 * Replace this with Room + Repository when wiring up the DB.
 *
 * Usage:  HabitStore.get().getHabits()
 */
public class HabitStore {

    private static HabitStore instance;
    private final List<Habit> habits = new ArrayList<>();
    private int nextId = 1;

    private HabitStore() {
        seedDemoData();
    }

    public static HabitStore get() {
        if (instance == null) instance = new HabitStore();
        return instance;
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

    public void add(Habit h) {
        h.id = nextId++;
        habits.add(h);
    }

    public void update(Habit updated) {
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).id == updated.id) { habits.set(i, updated); return; }
        }
    }

    public void delete(int id) {
        habits.removeIf(h -> h.id == id);
    }

    public Habit findById(int id) {
        for (Habit h : habits) if (h.id == id) return h;
        return null;
    }

    /** Toggle completedToday flag and update streak. */
    public void toggleComplete(int id) {
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
    }

    /** Count how many habits are completed today. */
    public int completedTodayCount() {
        int c = 0;
        for (Habit h : habits) if (h.completedToday) c++;
        return c;
    }

    // ── Seed demo habits ──────────────────────────────────────────────────────

    private void seedDemoData() {
        Habit h1 = new Habit(nextId++, "Morning Run",      "🏃", Habit.CAT_FITNESS,      Habit.PRIORITY_HIGH,   Habit.SEG_MORNING,   "#FF5252");
        h1.currentStreak = 7; h1.bestStreak = 14; h1.totalCompletions = 42;
        habits.add(h1);

        Habit h2 = new Habit(nextId++, "Read 20 Pages",    "📚", Habit.CAT_LEARNING,     Habit.PRIORITY_MEDIUM, Habit.SEG_MORNING,   "#FFD600");
        h2.currentStreak = 3; h2.bestStreak = 10; h2.totalCompletions = 18;
        habits.add(h2);

        Habit h3 = new Habit(nextId++, "Meditate 10 min",  "🧘", Habit.CAT_WELLNESS,     Habit.PRIORITY_MEDIUM, Habit.SEG_AFTERNOON, "#00BCD4");
        h3.currentStreak = 5; h3.bestStreak = 5; h3.totalCompletions = 20;
        habits.add(h3);

        Habit h4 = new Habit(nextId++, "Drink 8 Glasses",  "💧", Habit.CAT_NUTRITION,    Habit.PRIORITY_LOW,    Habit.SEG_AFTERNOON, "#7AD326");
        h4.currentStreak = 2; h4.bestStreak = 7; h4.totalCompletions = 15;
        habits.add(h4);

        Habit h5 = new Habit(nextId++, "Deep Work 2h",     "⚡", Habit.CAT_PRODUCTIVITY, Habit.PRIORITY_HIGH,   Habit.SEG_AFTERNOON, "#728AED");
        h5.currentStreak = 1; h5.bestStreak = 9; h5.totalCompletions = 30;
        habits.add(h5);

        Habit h6 = new Habit(nextId++, "Journal",          "✍️", Habit.CAT_WELLNESS,     Habit.PRIORITY_LOW,    Habit.SEG_EVENING,   "#9C6AE6");
        h6.currentStreak = 4; h6.bestStreak = 12; h6.totalCompletions = 22;
        habits.add(h6);
    }
}