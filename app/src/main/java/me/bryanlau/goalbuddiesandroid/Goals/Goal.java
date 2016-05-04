package me.bryanlau.goalbuddiesandroid.Goals;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Goal implements Parcelable {
    public String m_id, m_description, m_finished, m_eta, m_created, m_icon;
    public int m_type, m_version, m_times;
    public boolean m_unread, m_pending;

    public Goal(String id, int type, String description, int version,
                int times, String finished, String eta, String created,
                boolean unread, boolean pending, String icon) {
        m_id = id;
        m_type = type;
        m_description = description;
        m_version = version;
        m_times = times;
        m_finished = finished;
        m_eta = eta;
        m_created = created;
        m_unread = unread;
        m_pending = pending;
        m_icon = icon;
    }

    public Goal(JSONObject goal) {
        try {
            m_id = goal.getString("_id");
            m_type = goal.getInt("type");
            m_description = goal.getString("description");
            m_version = goal.getInt("version");
            m_times = goal.getInt("times");
            m_finished = goal.getString("finished");
            m_eta = goal.getString("eta");
            m_created = goal.getString("created");
            m_unread = goal.getBoolean("unread");
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
        m_eta = in.readString();
        m_created = in.readString();
        m_type = in.readInt();
        m_version = in.readInt();
        m_times = in.readInt();
        m_icon = in.readString();
        m_unread = in.readByte() != 0x00;
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
        dest.writeString(m_eta);
        dest.writeString(m_created);
        dest.writeInt(m_type);
        dest.writeInt(m_version);
        dest.writeInt(m_times);
        dest.writeString(m_icon);
        dest.writeByte((byte) (m_unread ? 0x01 : 0x00));
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