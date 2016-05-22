package me.bryanlau.goalbuddiesandroid.Social;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class User implements Parcelable {
    public String mUsername;
    public String mFirstName;
    public String mLastName;
    public String mCity;
    public int mGoalsCompleted;
    public int mTimesMotivated;
    public ArrayList<String> mFriends;
    public ArrayList<String> mIncoming;
    public ArrayList<String> mBlocked;

    public User(String username, String firstName, String lastName,
                String city, int goalsCompleted, int timesMotivated) {
        mUsername = username;
        mFirstName = firstName;
        mLastName = lastName;
        mCity = city;
        mGoalsCompleted = goalsCompleted;
        mTimesMotivated = timesMotivated;
        mFriends = new ArrayList<>();
        mIncoming = new ArrayList<>();
        mBlocked = new ArrayList<>();
    }

    protected User(Parcel in) {
        mUsername = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mCity = in.readString();
        mGoalsCompleted = in.readInt();
        mTimesMotivated = in.readInt();
        if (in.readByte() == 0x01) {
            mFriends = new ArrayList<String>();
            in.readList(mFriends, String.class.getClassLoader());
        } else {
            mFriends = null;
        }
        if (in.readByte() == 0x01) {
            mIncoming = new ArrayList<String>();
            in.readList(mIncoming, String.class.getClassLoader());
        } else {
            mIncoming = null;
        }
        if (in.readByte() == 0x01) {
            mBlocked = new ArrayList<String>();
            in.readList(mBlocked, String.class.getClassLoader());
        } else {
            mBlocked = null;
        }
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
        dest.writeInt(mTimesMotivated);
        if (mFriends == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mFriends);
        }
        if (mIncoming == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mIncoming);
        }
        if (mBlocked == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mBlocked);
        }
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