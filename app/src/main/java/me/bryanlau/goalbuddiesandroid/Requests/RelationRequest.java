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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class RelationRequest {
    private SharedPreferences preferences;
    private RequestQueue queue;
    private LocalBroadcastManager broadcastManager;
    private Intent relationIntent;

    public enum REQUEST_TYPE {
        REQUEST, ACCEPT, REJECT, CANCEL, BLOCK, UNFRIEND, UNBLOCK
    }

    private String username;
    private REQUEST_TYPE type;

    private Response.Listener<JSONObject> successListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                relationIntent.putExtra("statusCode", HttpURLConnection.HTTP_NO_CONTENT);
                relationIntent.putExtra("requestType", type);
                broadcastManager.sendBroadcast(relationIntent);
            }
        };
    }

    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                relationIntent.putExtra("statusCode", RequestUtils.getStatusCode(error));
                relationIntent.putExtra("error", RequestUtils.getUserError(error));

                // Log the error so we actually know what went wrong!
                Log.e("RelationRequest", RequestUtils.getDevError(error));
                broadcastManager.sendBroadcast(relationIntent);
            }
        };
    }

    public RelationRequest(Context context, String username, REQUEST_TYPE type) {
        // Set the shared preferences manager to edit in case login is successful
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        queue = Volley.newRequestQueue(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        this.username = username;
        this.type = type;

        relationIntent = new Intent(RequestUtils.relationAction);
    }

    public void execute() {
        String url = "http://goalbuddies.bryanlau.me/api/users/social";

        switch (type) {
            case REQUEST:
                url += "/request/";
                break;
            case ACCEPT:
                url += "/accept/";
                break;
            case REJECT:
                url += "/reject/";
                break;
            case CANCEL:
                url += "/cancel/";
                break;
            case BLOCK:
                url += "/block/";
                break;
            case UNFRIEND:
                url += "/unfriend/";
                break;
            case UNBLOCK:
                url += "/unblock/";
                break;
            default:
                relationIntent.putExtra("statusCode", HttpURLConnection.HTTP_BAD_REQUEST);
                broadcastManager.sendBroadcast(relationIntent);
                return;
        }

        url += username;

        JSONObject postParameters = new JSONObject();
        try {
            postParameters.put("token", preferences.getString("token", ""));

            JsonObjectRequest request = (JsonObjectRequest)
                    RequestUtils.setTimeout(
                            new JsonObjectRequest(
                                    Request.Method.POST,
                                    url,
                                    postParameters,
                                    successListener(),
                                    errorListener()
                            )
                    );

            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
