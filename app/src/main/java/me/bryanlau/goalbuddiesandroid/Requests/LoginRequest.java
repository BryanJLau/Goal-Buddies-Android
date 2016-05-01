package me.bryanlau.goalbuddiesandroid.Requests;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LoginRequest {
    private SharedPreferences preferences = null;
    private RequestQueue queue = null;
    LocalBroadcastManager broadcastManager = null;

    private Response.Listener<JSONObject> loginSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String token = (String) response.get("token");

                    // Save the token for future API calls
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token); // value to store
                    editor.apply();

                    // Send the broadcast so that the LoginActivity knows
                    Intent loginIntent = new Intent("goalbuddies.login");
                    loginIntent.putExtra("statusCode", HttpURLConnection.HTTP_OK);

                    broadcastManager.sendBroadcast(loginIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener loginErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent loginIntent = new Intent("goalbuddies.login");
                loginIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                loginIntent.putExtra("error", RequestUtils.getUserError(error));
                broadcastManager.sendBroadcast(loginIntent);
            }
        };
    }

    public LoginRequest(Context context) {
        // Set the shared preferences manager to edit in case login is successful
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void execute(Context context, String username, String password) {
        if(!RequestUtils.isNetworkAvailable(context)) {
            Intent loginIntent = new Intent("goalbuddies.login");
            loginIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
            loginIntent.putExtra("error", "Please enable your internet connection.");
            broadcastManager.sendBroadcast(loginIntent);
            return;
        }

        // Store the username and password for reuse on token expire
        // Okay if fails, because it'll be changed on re-execute
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();

        JSONObject postParameters = new JSONObject();
        try {
            postParameters.put("username", username);
            postParameters.put("password", password);

            JsonObjectRequest loginRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "http://goalbuddies.bryanlau.me/api/users/login",
                    postParameters,
                    loginSuccessListener(),
                    loginErrorListener()
            );

            queue.add(loginRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
