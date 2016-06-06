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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class AddGoalRequest {
    private SharedPreferences preferences;
    private RequestQueue queue;
    private LocalBroadcastManager broadcastManager;
    private Intent addGoalIntent;

    private String description;
    private int type;
    private String icon;
    private int daysToFinish;

    private Response.Listener<String> successListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                addGoalIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);
                broadcastManager.sendBroadcast(addGoalIntent);
            }
        };
    }

    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                addGoalIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                addGoalIntent.putExtra("error", RequestUtils.getUserError(error));

                // Log the error so we actually know what went wrong!
                Log.e("AddGoalRequest", RequestUtils.getDevError(error));
                broadcastManager.sendBroadcast(addGoalIntent);
            }
        };
    }

    public AddGoalRequest(Context context, String description, String spinnerType,
                          String spinnerIcon, String daysToFinish) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        this.description = description;
        this.type = spinnerType.equals("Recurring") ? 0 : 1;
        this.icon = spinnerIcon.replace(" ", "-").toLowerCase();
        this.daysToFinish = Integer.parseInt(daysToFinish);

        addGoalIntent = new Intent(RequestUtils.addGoalAction);
    }

    public void execute() {
        String url = "http://goalbuddies.bryanlau.me/api/goals/";

        StringRequest request = (StringRequest)
                RequestUtils.setTimeout(
                        new StringRequest(
                                Request.Method.POST,
                                url,
                                successListener(),
                                errorListener()
                        ) {
                            protected Map<String, String> getParams()
                                    throws com.android.volley.AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("token", preferences.getString("token", ""));
                                params.put("description", description);
                                params.put("type", Integer.toString(type));
                                params.put("icon", icon);
                                params.put("daysToFinish", Integer.toString(daysToFinish));

                                return params;
                            }
                        }
                );

        queue.add(request);
    }
}
