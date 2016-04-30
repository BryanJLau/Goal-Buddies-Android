package me.bryanlau.goalbuddiesandroid.Goals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public enum GoalContainer {
    INSTANCE;

    // Want linked for order preservation, already sorted by eta
    private Map<String, Goal> pendingRecurring = new LinkedHashMap<>();
    private Map<String, Goal> pendingOneTime = new LinkedHashMap<>();
    private Map<String, Goal> finishedDaily = new LinkedHashMap<>();
    private Map<String, Goal> finishedOneTime = new LinkedHashMap<>();

    public ArrayList<Goal> getPendingRecurring() {
        return new ArrayList<>(pendingRecurring.values());
    }

    public ArrayList<Goal> getPendingOneTime() {
        return new ArrayList<>(pendingOneTime.values());
    }

    public ArrayList<Goal> getFinishedRecurring() {
        return new ArrayList<>(finishedDaily.values());
    }

    public ArrayList<Goal> getFinishedOneTime() {
        return new ArrayList<>(finishedOneTime.values());
    }
}
