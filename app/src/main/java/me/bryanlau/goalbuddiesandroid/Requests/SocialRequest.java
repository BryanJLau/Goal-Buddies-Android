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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Social.SocialContainer;

public class SocialRequest {
    private SharedPreferences preferences = null;
    private RequestQueue queue = null;
    LocalBroadcastManager broadcastManager = null;

    private boolean friendsSuccess;
    private boolean incomingSuccess;
    private boolean blockedSuccess;
    private boolean encounteredError;

    private void sendBroadcast() {
        if(friendsSuccess && incomingSuccess && blockedSuccess) {
            Intent socialIntent = new Intent("goalbuddies.social");
            socialIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
            broadcastManager.sendBroadcast(socialIntent);
        }
    }

    private Response.Listener<JSONArray> friendsSuccessListener() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if(!encounteredError) {
                        ArrayList<String> friendsList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            friendsList.add(response.getString(i));
                        }

                        SocialContainer.INSTANCE.friends = friendsList;
                        friendsSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.social");
                        goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                        broadcastManager.sendBroadcast(goalListIntent);
                    }
                } finally {
                    sendBroadcast();
                }
            }
        };
    }

    private Response.Listener<JSONArray> incomingSuccessListener() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if(!encounteredError) {
                        ArrayList<String> pendingList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            pendingList.add(response.getString(i));
                        }

                        SocialContainer.INSTANCE.pending = pendingList;
                        incomingSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.social");
                        goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                        broadcastManager.sendBroadcast(goalListIntent);
                    }
                } finally {
                    sendBroadcast();
                }
            }
        };
    }

    private Response.Listener<JSONArray> blockedSuccessListener() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if(!encounteredError) {
                        ArrayList<String> blockedList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            blockedList.add(response.getString(i));
                        }

                        SocialContainer.INSTANCE.blocked = blockedList;
                        blockedSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.social");
                        goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                        broadcastManager.sendBroadcast(goalListIntent);
                    }
                } finally {
                    sendBroadcast();
                }
            }
        };
    }

    private Response.ErrorListener errorListener() {
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

    public SocialRequest(Context context) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        friendsSuccess = incomingSuccess = blockedSuccess = encounteredError = false;
    }

    public void execute() {
        JsonArrayRequest friendsRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/users/social/friends",
                null,
                friendsSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        JsonArrayRequest incomingRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/users/social/incoming",
                null,
                incomingSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        JsonArrayRequest blockedRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/users/social/blocked",
                null,
                blockedSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        queue.add(friendsRequest);
        queue.add(incomingRequest);
        queue.add(blockedRequest);
    }
}
