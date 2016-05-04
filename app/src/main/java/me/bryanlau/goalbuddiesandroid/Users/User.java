package me.bryanlau.goalbuddiesandroid.Users;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public String mUsername;
    public String mFirstName;
    public String mLastName;
    public String mCity;
    public int mGoalsCompleted;

    public User(String username, String firstName, String lastName,
                String city, int goalsCompleted) {
        mUsername = username;
        mFirstName = firstName;
        mLastName = lastName;
        mCity = city;
        mGoalsCompleted = goalsCompleted;
    }

    protected User(Parcel in) {
        mUsername = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mCity = in.readString();
        mGoalsCompleted = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mCity);
        dest.writeInt(mGoalsCompleted);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}