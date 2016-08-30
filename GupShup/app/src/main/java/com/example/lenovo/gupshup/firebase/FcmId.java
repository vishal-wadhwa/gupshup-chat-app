package com.example.lenovo.gupshup.firebase;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.activities.LoginActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lenovo on 12-Aug-16.
 */
public class FcmId extends FirebaseInstanceIdService {

    private static final String TAG = "FcmId";
    public static final String FCM_TOKEN_SAVE = "token_save";
    private String token = null;

    public FcmId() {
        super();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        token = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(token);
        //update token on server
    }


    private void sendTokenToServer(final String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(FCM_TOKEN_SAVE, MODE_PRIVATE);
        if (!pref.getString(LoginActivity.USER_PHONE, "").isEmpty()) {
            String phone = pref.getString(LoginActivity.USER_PHONE, "");
            StringRequest req = new StringRequest(
                    Request.Method.PUT,
                    URLEndPoints.EndPoints.UPDATE_FCM_ID + phone,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d(FcmId.FCM_TOKEN_SAVE, "Token Update from fcmid: " + new JSONObject(response).getString("message"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error occurred " + error.networkResponse.statusCode + " " + error.getMessage());
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("gcm_id", token);
                    Log.d(TAG, "Map: " + map);
                    return map;
                }

                @Override
                public String getBodyContentType() {
                    Log.d(TAG, "ContentBodyTypefcm");
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

            };
            Volley.newRequestQueue(getApplicationContext()).add(req);

        }
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(LoginActivity.USER_TOKEN, token);
            editor.apply();

    }
}


