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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bryanlau.goalbuddiesandroid.Goals.Goal;
import me.bryanlau.goalbuddiesandroid.Users.User;

public class ProfileRequest {
    private SharedPreferences preferences = null;
    private RequestQueue queue = null;
    LocalBroadcastManager broadcastManager = null;

    private boolean userSuccess;
    private boolean recurringGoalsSuccess;
    private boolean onetimeGoalsSuccess;
    private boolean encounteredError;

    public enum RELATION {
        NONE, FRIENDS, INCOMING, OUTGOING
    }

    private String mUsername;
    private User user;
    private ArrayList<Goal> recurring;
    private ArrayList<Goal> onetime;
    private RELATION relation;

    private void sendBroadcast() {
        if(userSuccess && recurringGoalsSuccess && onetimeGoalsSuccess) {
            Intent profileIntent = new Intent("goalbuddies.profile");
            profileIntent.putExtra("user", user);
            profileIntent.putExtra("recurring", recurring);
            profileIntent.putExtra("onetime", onetime);
            profileIntent.putExtra("relation", relation);
            profileIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
            broadcastManager.sendBroadcast(profileIntent);
        }
    }

    private Response.Listener<JSONObject> userSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(!encounteredError) {
                        JSONObject jsonUser = response.getJSONObject("user");
                        user = new User(
                                jsonUser.getString("username"),
                                jsonUser.getString("firstName"),
                                jsonUser.getString("lastName"),
                                jsonUser.getString("city"),
                                jsonUser.getInt("goalsCompleted")
                        );

                        // These arrays only have 0 or 1 item
                        JSONArray friendsArray = jsonUser.getJSONArray("friends");
                        JSONArray incomingArray = jsonUser.getJSONArray("incoming");
                        JSONArray outgoingArray = jsonUser.getJSONArray("outgoing");

                        if (friendsArray.length() == 1) {
                            relation = RELATION.FRIENDS;
                        } else if (incomingArray.length() == 1) {
                            // Their incoming = your outgoing
                            relation = RELATION.OUTGOING;
                        } else if (outgoingArray.length() == 1) {
                            relation = RELATION.INCOMING;
                        } else {
                            relation = RELATION.NONE;
                        }

                        userSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.profile");
                        goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                        broadcastManager.sendBroadcast(goalListIntent);
                    }
                } finally {
                    sendBroadcast();
                }
            }
        };
    }

    private Response.Listener<JSONObject> recurringGoalsSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(!encounteredError) {
                        recurring = new ArrayList<>();
                        JSONArray goalsArray = response.getJSONArray("goals");

                        for (int i = 0; i < goalsArray.length(); i++) {
                            recurring.add(new Goal(goalsArray.getJSONObject(i)));
                        }

                        recurringGoalsSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.profile");
                        goalListIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                        broadcastManager.sendBroadcast(goalListIntent);
                    }
                } finally {
                    sendBroadcast();
                }
            }
        };
    }

    private Response.Listener<JSONObject> onetimeGoalsSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(!encounteredError) {
                        onetime = new ArrayList<>();
                        JSONArray goalsArray = response.getJSONArray("goals");

                        for (int i = 0; i < goalsArray.length(); i++) {
                            onetime.add(new Goal(goalsArray.getJSONObject(i)));
                        }

                        onetimeGoalsSuccess = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    // We want to do this on the FIRST occurrence of an error,
                    // when encounteredError is still false
                    if(!encounteredError) {
                        encounteredError = true;

                        Intent goalListIntent = new Intent("goalbuddies.profile");
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
                Intent goalListIntent = new Intent("goalbuddies.profile");
                goalListIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                goalListIntent.putExtra("error", RequestUtils.getUserError(error));
                broadcastManager.sendBroadcast(goalListIntent);
            }
        };
    }

    public ProfileRequest(Context context, String username) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        userSuccess = recurringGoalsSuccess =
                onetimeGoalsSuccess = encounteredError = false;

        mUsername = username;
    }

    public void execute() {
        JsonObjectRequest userRequest = new JsonObjectRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/users/search/" + mUsername,
                null,
                userSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        JsonObjectRequest recurringGoalsRequest = new JsonObjectRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/goals/list?username=" +
                    mUsername,
                null,
                recurringGoalsSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        JsonObjectRequest onetimeGoalsRequest = new JsonObjectRequest(
                Request.Method.GET,
                "http://goalbuddies.bryanlau.me/api/goals/list?username=" +
                        mUsername + "&type=1",
                null,
                onetimeGoalsSuccessListener(),
                errorListener()
        ) {
            public Map<String, String> getHeaders() throws
                    com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", preferences.getString("token", ""));
                return params;
            }
        };

        queue.add(userRequest);
        queue.add(recurringGoalsRequest);
        queue.add(onetimeGoalsRequest);
    }
}
