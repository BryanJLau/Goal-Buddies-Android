package me.bryanlau.goalbuddiesandroid.Social;

import java.util.ArrayList;

public enum SocialContainer {
    INSTANCE;

    public ArrayList<String> friends = new ArrayList<>();
    public ArrayList<String> incoming = new ArrayList<>();
    public ArrayList<String> blocked = new ArrayList<>();

    public String mUsername;
    public String mFirstName;
    public String mLastName;
    public String mCity;
    public int mGoalsCompleted;
    public int mTimesMotivated;
}
