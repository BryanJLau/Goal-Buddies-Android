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

public class MotivationRequest {
    private SharedPreferences preferences;
    private RequestQueue queue;
    private LocalBroadcastManager broadcastManager;
    private Intent motivationIntent;

    private String username;
    private String id;

    private Response.Listener<String> successListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                motivationIntent.putExtra("statusCode", HttpURLConnection.HTTP_NO_CONTENT);
                broadcastManager.sendBroadcast(motivationIntent);
            }
        };
    }

    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                motivationIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                motivationIntent.putExtra("error", RequestUtils.getUserError(error));

                // Log the error so we actually know what went wrong!
                Log.e("MotivationRequest", RequestUtils.getDevError(error));
                broadcastManager.sendBroadcast(motivationIntent);
            }
        };
    }

    public MotivationRequest(Context context, String username, String id) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        this.username = username;
        this.id = id;

        motivationIntent = new Intent(RequestUtils.motivationAction);
    }

    public void execute() {
        String url = "http://goalbuddies.bryanlau.me/api/goals/" +
                username + "/" + id + "/motivate";

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
                                return params;
                            }
                        }
                );

        queue.add(request);
    }
}
