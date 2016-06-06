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
    private Intent goalListIntent;

    // Optional Parameters
    private final String username;

    private Response.Listener<JSONObject> goalListSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray pendingRecurring = response.getJSONArray("pendingRecurring");
                    ArrayList<Goal> pendingRecurringArrayList = new ArrayList<>();
                    for (int i = 0; i < pendingRecurring.length(); i++) {
                        pendingRecurringArrayList.add(
                                new Goal((JSONObject) pendingRecurring.get(i))
                        );
                    }
                    goalListIntent.putParcelableArrayListExtra(
                            "pendingRecurring", pendingRecurringArrayList);

                    JSONArray pendingOneTime = response.getJSONArray("pendingOneTime");
                    ArrayList<Goal> pendingOneTimeArrayList = new ArrayList<>();
                    for (int i = 0; i < pendingOneTime.length(); i++) {
                        pendingOneTimeArrayList.add(
                                new Goal((JSONObject) pendingOneTime.get(i))
                        );
                    }
                    goalListIntent.putParcelableArrayListExtra(
                            "pendingOneTime", pendingOneTimeArrayList);

                    if (username.equals("")) {
                        JSONArray finishedRecurring = response.getJSONArray("finishedRecurring");
                        ArrayList<Goal> finishedRecurringArrayList = new ArrayList<>();
                        for (int i = 0; i < finishedRecurring.length(); i++) {
                            pendingRecurringArrayList.add(
                                    new Goal((JSONObject) finishedRecurring.get(i))
                            );
                        }
                        goalListIntent.putParcelableArrayListExtra(
                                "finishedRecurring", finishedRecurringArrayList);

                        JSONArray finishedOneTime = response.getJSONArray("finishedOneTime");
                        ArrayList<Goal> finishedOneTimeArrayList = new ArrayList<>();
                        for (int i = 0; i < finishedOneTime.length(); i++) {
                            finishedOneTimeArrayList.add(
                                    new Goal((JSONObject) finishedOneTime.get(i))
                            );
                        }
                        goalListIntent.putParcelableArrayListExtra(
                                "finishedOneTime", finishedOneTimeArrayList);

                        JSONArray major = response.getJSONArray("major");
                        ArrayList<Goal> majorArrayList = new ArrayList<>();
                        for (int i = 0; i < major.length(); i++) {
                            majorArrayList.add(
                                    new Goal((JSONObject) major.get(i))
                            );
                        }
                        goalListIntent.putParcelableArrayListExtra(
                                "major", majorArrayList);
                    }

                    // Send the broadcast so that the MainActivity knows
                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
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

        public Builder(Context context) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            queue = Volley.newRequestQueue(context);
            broadcastManager = LocalBroadcastManager.getInstance(context);

            username = "";
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public GoalListRequest build() {
            return new GoalListRequest(this);
        }
    }

    private GoalListRequest(Builder builder) {
        // Required parameters
        preferences = builder.preferences;
        queue = builder.queue;
        broadcastManager = builder.broadcastManager;

        // Optional parameters
        username = builder.username;

        goalListIntent = new Intent(RequestUtils.goalListAction);
    }

    public void execute() {
        // Build the URL based on the parameters given
        String url = "http://goalbuddies.bryanlau.me/api/goals/list/" + username;

        JsonObjectRequest goalListRequest = (JsonObjectRequest)
                RequestUtils.setTimeout(
                        new JsonObjectRequest(
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
                        }
                );

        queue.add(goalListRequest);
    }
}
