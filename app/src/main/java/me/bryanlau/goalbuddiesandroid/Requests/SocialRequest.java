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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Social.SocialContainer;

public class SocialRequest {
    private SharedPreferences preferences = null;
    private RequestQueue queue = null;
    LocalBroadcastManager broadcastManager = null;

    private Response.Listener<JSONObject> successListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject userObject = response.getJSONObject("user");
                    SocialContainer.INSTANCE.mUsername = userObject.getString("username");
                    SocialContainer.INSTANCE.mCity = userObject.getString("city");
                    SocialContainer.INSTANCE.mFirstName = userObject.getString("firstName");
                    SocialContainer.INSTANCE.mLastName = userObject.getString("lastName");
                    SocialContainer.INSTANCE.mGoalsCompleted = userObject.getInt("goalsCompleted");
                    SocialContainer.INSTANCE.mTimesMotivated = userObject.getInt("timesMotivated");

                    JSONArray friends = userObject.getJSONArray("friends");
                    ArrayList<String> friendsList = new ArrayList<>();
                    for (int i = 0; i < friends.length(); i++) {
                        friendsList.add(friends.getString(i));
                    }
                    SocialContainer.INSTANCE.friends = friendsList;

                    JSONArray incoming = userObject.getJSONArray("incoming");
                    ArrayList<String> incomingList = new ArrayList<>();
                    for (int i = 0; i < incoming.length(); i++) {
                        incomingList.add(incoming.getString(i));
                    }
                    SocialContainer.INSTANCE.incoming = incomingList;

                    JSONArray blocked = userObject.getJSONArray("blocked");
                    ArrayList<String> blockedList = new ArrayList<>();
                    for (int i = 0; i < blocked.length(); i++) {
                        blockedList.add(blocked.getString(i));
                    }
                    SocialContainer.INSTANCE.blocked = blockedList;

                    Intent goalListIntent = new Intent("goalbuddies.social");
                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
                    broadcastManager.sendBroadcast(goalListIntent);
                } catch (JSONException e) {
                    e.printStackTrace();

                    Intent goalListIntent = new Intent("goalbuddies.social");
                    goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                    broadcastManager.sendBroadcast(goalListIntent);
                }
            }
        };
    }
    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent goalListIntent = new Intent("goalbuddies.social");
                goalListIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                goalListIntent.putExtra("error", RequestUtils.getUserError(error));
                broadcastManager.sendBroadcast(goalListIntent);
            }
        };
    }

    public SocialRequest(Context context) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void execute() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/users/search",
                null,
                successListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        queue.add(request);
    }
}
