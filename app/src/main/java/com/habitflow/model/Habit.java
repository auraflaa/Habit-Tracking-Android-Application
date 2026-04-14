package com.habitflow.model;

import java.io.Serializable;
import java.util.UUID;

public class Habit implements Serializable {
    public static final String CAT_FITNESS = "Fitness";
    public static final String CAT_LEARNING = "Learning";
    public static final String CAT_WELLNESS = "Wellness";
    public static final String CAT_NUTRITION = "Nutrition";
    public static final String CAT_PRODUCTIVITY = "Productivity";
    public static final String CAT_SOCIAL = "Social";

    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_LOW = "Low";

    public static final String SEG_MORNING = "Morning";
    public static final String SEG_AFTERNOON = "Afternoon";
    public static final String SEG_EVENING = "Evening";

    public String id; // Changed from int to String for Backend/UUID compatibility
    public String name;
    public String description = "";
    public String emoji;
    public String colorHex;
    public String category;
    public String priority;
    public String segment;
    public boolean notifyEnabled;
    public String notifyTime = "";
    
    public int currentStreak = 0;
    public int bestStreak = 0;
    public int totalCompletions = 0;
    public boolean completedToday = false;

    public Habit() {
        this.id = UUID.randomUUID().toString(); // Default ID generation
    }

    public Habit(String id, String name, String emoji, String category, String priority, String segment, String colorHex) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.category = category;
        this.priority = priority;
        this.segment = segment;
        this.colorHex = colorHex;
    }
}
