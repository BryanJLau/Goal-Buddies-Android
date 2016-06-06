package me.bryanlau.goalbuddiesandroid.Goals;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Goal implements Parcelable {
    public String m_id, m_description, m_finished, m_created, m_lastDate, m_icon;
    public int m_type, m_times;
    public boolean m_pending;

    public Goal(JSONObject goal) {
        try {
            JSONObject dates = goal.getJSONObject("dates");
            m_created = dates.getString("created");
            m_finished = dates.getString("finished");
            m_lastDate = dates.getString("lastDate");

            m_id = goal.getString("_id");
            m_type = goal.getInt("type");
            m_description = goal.getString("description");
            m_times = goal.getInt("times");
            m_pending = goal.getBoolean("pending");
            m_icon = goal.getString("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Goal(Parcel in) {
        m_id = in.readString();
        m_description = in.readString();
        m_finished = in.readString();
        m_created = in.readString();
        m_lastDate = in.readString();
        m_icon = in.readString();
        m_type = in.readInt();
        m_times = in.readInt();
        m_pending = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(m_id);
        dest.writeString(m_description);
        dest.writeString(m_finished);
        dest.writeString(m_created);
        dest.writeString(m_lastDate);
        dest.writeString(m_icon);
        dest.writeInt(m_type);
        dest.writeInt(m_times);
        dest.writeByte((byte) (m_pending ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Goal> CREATOR = new Parcelable.Creator<Goal>() {
        @Override
        public Goal createFromParcel(Parcel in) {
            return new Goal(in);
        }

        @Override
        public Goal[] newArray(int size) {
            return new Goal[size];
        }
    };
}