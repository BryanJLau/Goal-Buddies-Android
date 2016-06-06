package me.bryanlau.goalbuddiesandroid.Requests;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public final class RequestUtils {
    private RequestUtils() {
    }    // Purely static class

    public static String goalListAction = "goalbuddies.goalList";
    public static String profileAction = "goalbuddies.profile";
    public static String relationAction = "goalbuddies.relation";
    public static String motivationAction = "goalbuddies.motivation";
    public static String addGoalAction = "goalbuddies.addGoal";
    public static IntentFilter goalListFilter = new IntentFilter(goalListAction);
    public static IntentFilter profileFilter = new IntentFilter(profileAction);
    public static IntentFilter relationFilter = new IntentFilter(relationAction);
    public static IntentFilter motivationFilter = new IntentFilter(motivationAction);
    public static IntentFilter addGoalFilter = new IntentFilter(addGoalAction);

    public static boolean isOk(int statusCode) {
        return (statusCode >= 200 && statusCode < 300);
    }

    public static boolean isBad(int statusCode) {
        return (statusCode >= 400 && statusCode < 500);
    }

    public static Request setTimeout(Request request) {
        return request.setRetryPolicy(
                new DefaultRetryPolicy(20000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int getStatusCode(VolleyError error) {
        try {
            JSONObject response = new JSONObject(new String(error.networkResponse.data));
            return response.getInt("statusCode");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Default to bad response
        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

    @Nullable
    public static String getDevError(VolleyError error) {
        try {
            JSONObject response = new JSONObject(new String(error.networkResponse.data));
            return response.getString("devError");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Default to bad response
        return null;
    }

    @Nullable
    public static String getUserError(VolleyError error) {
        try {
            JSONObject response = new JSONObject(new String(error.networkResponse.data));
            return response.getString("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Default to bad response
        return null;
    }
}
