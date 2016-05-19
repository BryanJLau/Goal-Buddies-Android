package me.bryanlau.goalbuddiesandroid.Social;

import java.util.ArrayList;

public enum SocialContainer {
    INSTANCE;

    public ArrayList<String> friends = new ArrayList<>();
    public ArrayList<String> incoming = new ArrayList<>();
    public ArrayList<String> blocked = new ArrayList<>();
}
