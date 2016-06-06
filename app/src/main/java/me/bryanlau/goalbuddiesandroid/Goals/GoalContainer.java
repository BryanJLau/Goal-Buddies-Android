package me.bryanlau.goalbuddiesandroid.Goals;

import android.os.Parcel;
import android.os.Parcelable;

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
    private Map<String, Goal> major = new LinkedHashMap<>();

    // Return type for chaining
    public GoalContainer setPendingRecurring(ArrayList<Goal> goals) {
        pendingRecurring.clear();
        for(int i = 0; i < goals.size(); i++) {
            pendingRecurring.put(goals.get(i).m_id, goals.get(i));
        }
        return this;
    }
    public ArrayList<Goal> getPendingRecurring() {
        return new ArrayList<>(pendingRecurring.values());
    }

    public GoalContainer setPendingOneTime(ArrayList<Goal> goals) {
        pendingOneTime.clear();
        for(int i = 0; i < goals.size(); i++) {
            pendingOneTime.put(goals.get(i).m_id, goals.get(i));
        }
        return this;
    }
    public ArrayList<Goal> getPendingOneTime() {
        return new ArrayList<>(pendingOneTime.values());
    }

    public GoalContainer setFinishedRecurring(ArrayList<Goal> goals) {
        finishedRecurring.clear();
        for(int i = 0; i < goals.size(); i++) {
            finishedRecurring.put(goals.get(i).m_id, goals.get(i));
        }
        return this;
    }
    public ArrayList<Goal> getFinishedRecurring() {
        return new ArrayList<>(finishedRecurring.values());
    }

    public GoalContainer setFinishedOneTime(ArrayList<Goal> goals) {
        finishedOneTime.clear();
        for(int i = 0; i < goals.size(); i++) {
            finishedOneTime.put(goals.get(i).m_id, goals.get(i));
        }
        return this;
    }
    public ArrayList<Goal> getFinishedOneTime() {
        return new ArrayList<>(finishedOneTime.values());
    }

    public GoalContainer setMajor(ArrayList<Goal> goals) {
        major.clear();
        for(int i = 0; i < goals.size(); i++) {
            major.put(goals.get(i).m_id, goals.get(i));
        }
        return this;
    }
    public ArrayList<Goal> getMajor() {
        return new ArrayList<>(major.values());
    }
}
