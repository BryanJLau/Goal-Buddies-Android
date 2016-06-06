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
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Social.SocialContainer;
import me.bryanlau.goalbuddiesandroid.Social.User;

public class ProfileRequest {
    private SharedPreferences preferences;
    private RequestQueue queue;
    private LocalBroadcastManager broadcastManager;
    private Intent profileIntent;

    public enum RELATION {
        NONE, SELF, FRIENDS, INCOMING, OUTGOING, BLOCKING
    }

    private String username;
    private User user;
    private RELATION relation;

    private Response.Listener<JSONObject> successListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonUser = response.getJSONObject("user");
                    JSONObject personal = jsonUser.getJSONObject("personal");
                    JSONObject statistics = jsonUser.getJSONObject("statistics");

                    user = new User(
                            jsonUser.getString("username"),
                            personal.getString("firstName"),
                            personal.getString("lastName"),
                            personal.getString("city"),
                            statistics.getInt("goalsCompleted"),
                            statistics.getInt("motivationsGiven")
                    );

                    JSONObject relationships = jsonUser.getJSONObject("relationships");
                    // These arrays only have 0 or 1 item
                    JSONArray friendsArray = relationships.getJSONArray("friends");
                    JSONArray incomingArray = relationships.getJSONArray("incoming");
                    JSONArray outgoingArray = relationships.getJSONArray("outgoing");

                    if(username.equals(preferences.getString("username", ""))) {
                        for (int i = 0; i < friendsArray.length(); i++) {
                            user.mFriends.add((String) friendsArray.get(i));
                        }
                        for (int i = 0; i < incomingArray.length(); i++) {
                            user.mIncoming.add((String) incomingArray.get(i));
                        }

                        JSONArray blockedArray = relationships.getJSONArray("blocking");
                        for (int i = 0; i < blockedArray.length(); i++) {
                            user.mBlocked.add((String) blockedArray.get(i));
                        }

                        relation = RELATION.SELF;
                    } else if (SocialContainer.INSTANCE.blocked.contains(username)) {
                        relation = RELATION.BLOCKING;
                    } else if (friendsArray.length() == 1) {
                        relation = RELATION.FRIENDS;
                    } else if (incomingArray.length() == 1) {
                        // Their incoming = your outgoing
                        relation = RELATION.OUTGOING;
                    } else if (outgoingArray.length() == 1) {
                        relation = RELATION.INCOMING;
                    } else {
                        relation = RELATION.NONE;
                    }

                    profileIntent.putExtra("user", user);
                    profileIntent.putExtra("relation", relation);
                    profileIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
                    broadcastManager.sendBroadcast(profileIntent);
                } catch (JSONException e) {
                    e.printStackTrace();

                    profileIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                    broadcastManager.sendBroadcast(profileIntent);
                }
            }
        };
    }
    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                profileIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                profileIntent.putExtra("error", RequestUtils.getUserError(error));

                // Log the error so we actually know what went wrong!
                Log.e("ProfileRequest", RequestUtils.getDevError(error));
                broadcastManager.sendBroadcast(profileIntent);
            }
        };
    }

    public ProfileRequest(Context context, String username) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        this.username = username;

        profileIntent = new Intent(RequestUtils.profileAction);
    }

    public ProfileRequest(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        this.username = preferences.getString("username", "");

        profileIntent = new Intent(RequestUtils.profileAction);
    }

    public void execute() {
        String url = "http://goalbuddies.bryanlau.me/api/users/search/" + username;

        JsonObjectRequest request = (JsonObjectRequest)
                RequestUtils.setTimeout(
                        new JsonObjectRequest(
                                Request.Method.GET,
                                url,
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
                        }
                );

        queue.add(request);
    }
}
