package me.bryanlau.goalbuddiesandroid.Goals;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public enum GoalContainer {
    INSTANCE;

    // Want linked for order preservation, already sorted by eta
    private Map<String, Goal> pendingRecurring = new LinkedHashMap<>();
    private Map<String, Goal> pendingOneTime = new LinkedHashMap<>();
    private Map<String, Goal> finishedRecurring = new LinkedHashMap<>();
    private Map<String, Goal> finishedOneTime = new LinkedHashMap<>();

    public int version = 0;

    public ArrayList<Goal> getPendingRecurring() {
        return new ArrayList<>(pendingRecurring.values());
    }

    public ArrayList<Goal> getPendingOneTime() {
        return new ArrayList<>(pendingOneTime.values());
    }

    public ArrayList<Goal> getFinishedRecurring() {
        return new ArrayList<>(finishedRecurring.values());
    }

    public ArrayList<Goal> getFinishedOneTime() {
        return new ArrayList<>(finishedOneTime.values());
    }

    public void addGoal(JSONObject goal) {
        try {
            String id = goal.getString("_id");
            int type = goal.getInt("type");
            String description = goal.getString("description");
            int version = goal.getInt("version");
            int times = goal.getInt("times");
            String finished = goal.getString("finished");
            String eta = goal.getString("eta");
            String created = goal.getString("created");
            boolean unread = goal.getBoolean("unread");
            boolean pending = goal.getBoolean("pending");
            String icon = goal.getString("icon");

            Goal g = new Goal(id, type, description, version, times,
                    finished, eta, created, unread, pending, icon);

            if(pending) {
                if(type == 0) {
                    pendingRecurring.put(id, g);
                } else {
                    pendingOneTime.put(id, g);
                }
            } else {
                if(type == 0) {
                    finishedRecurring.put(id, g);
                } else {
                    finishedOneTime.put(id, g);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
