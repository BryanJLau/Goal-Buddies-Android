package me.bryanlau.goalbuddiesandroid.Requests;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Goals.Goal;

public class GoalListRequest {
    private SharedPreferences preferences;
    private RequestQueue queue;
    private LocalBroadcastManager broadcastManager;
    private Intent goalListIntent = new Intent("goalbuddies.goalList");

    // Optional Parameters
    private final String username;
    private final boolean pending;
    private final int type;
    private final int limit;
    private final int offset;
    private final int version;

    private Response.Listener<JSONObject> goalListSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ArrayList<Goal> goalArrayList = new ArrayList<>();
                    JSONArray goalsArray = response.getJSONArray("goals");

                    for (int i = 0; i < goalsArray.length(); i++) {
                        goalArrayList.add(new Goal(goalsArray.getJSONObject(i)));
                    }

                    // Send the broadcast so that the MainActivity knows
                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
                    goalListIntent.putExtra("goalList", goalArrayList);
                    broadcastManager.sendBroadcast(goalListIntent);
                } catch (JSONException e) {
                    e.printStackTrace();

                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                    broadcastManager.sendBroadcast(goalListIntent);
                }
            }
        };
    }

    private Response.ErrorListener goalListErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                goalListIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                goalListIntent.putExtra("error", RequestUtils.getUserError(error));

                // Log the error so we actually know what went wrong!
                Log.e("GoalListRequest", RequestUtils.getDevError(error));

                broadcastManager.sendBroadcast(goalListIntent);
            }
        };
    }

    public static class Builder {
        private SharedPreferences preferences;
        private RequestQueue queue;
        private LocalBroadcastManager broadcastManager;

        private String username;
        private boolean pending = true;
        private int type = 0;
        private int limit = 10;
        private int offset = 0;
        private int version= 0;

        public Builder(Context context) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            queue = Volley.newRequestQueue(context);
            broadcastManager = LocalBroadcastManager.getInstance(context);

            username = preferences.getString("username", "");
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder pending(boolean pending) {
            this.pending = pending;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public GoalListRequest build() {
            return new GoalListRequest(this);
        }
    }

    private GoalListRequest(Builder builder) {
        // Required parameters
        preferences         = builder.preferences;
        queue               = builder.queue;
        broadcastManager    = builder.broadcastManager;

        // Optional parameters
        username    = builder.username;
        pending     = builder.pending;
        type        = builder.type;
        limit       = builder.limit;
        offset      = builder.offset;
        version     = builder.version;
    }

    public void execute() {
        // Build the URL based on the parameters given
        String url = "http://goalbuddies.bryanlau.me/api/goals/list?" +
                "username=" + username +
                "&pending=" + Boolean.toString(pending) +
                "&type=" + Integer.toString(type) +
                "&limit=" + Integer.toString(limit) +
                "&offset=" + Integer.toString(offset) +
                "&version=" + Integer.toString(version);

        JsonObjectRequest goalListRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                goalListSuccessListener(),
                goalListErrorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        queue.add(goalListRequest);
    }
}
