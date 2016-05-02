package me.bryanlau.goalbuddiesandroid.Requests;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

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
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Goals.GoalContainer;

public class GoalListRequest {
    private SharedPreferences preferences = null;
    private RequestQueue queue = null;
    LocalBroadcastManager broadcastManager = null;

    private Response.Listener<JSONObject> goalListSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray goalsArray = response.getJSONArray("goals");

                    for(int i = 0; i < goalsArray.length(); i++) {
                        JSONObject goal = goalsArray.getJSONObject(i);

                        GoalContainer.INSTANCE.addGoal(goal);
                    }

                    // Send the broadcast so that the MainActivity knows
                    Intent goalListIntent = new Intent("goalbuddies.goalList");
                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
                    broadcastManager.sendBroadcast(goalListIntent);
                } catch (JSONException e) {
                    e.printStackTrace();

                    Intent goalListIntent = new Intent("goalbuddies.goalList");
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
                Intent goalListIntent = new Intent("goalbuddies.goalList");
                goalListIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                goalListIntent.putExtra("error", RequestUtils.getUserError(error));
                broadcastManager.sendBroadcast(goalListIntent);
            }
        };
    }

    public GoalListRequest(Context context) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void execute() {
        JsonObjectRequest goalListRequest = new JsonObjectRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/goals/list?all=true",
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
