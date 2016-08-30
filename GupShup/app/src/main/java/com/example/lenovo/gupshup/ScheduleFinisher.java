package com.example.lenovo.gupshup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.activities.LoginActivity;
import com.example.lenovo.gupshup.db.AwaitList;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.firebase.URLEndPoints;

import java.util.HashMap;
import java.util.Map;

public class ScheduleFinisher extends Service {
    private static final String TAG = "ScheduleFinisher";
    NetCheck netCheck;
    IntentFilter iF;
    public ScheduleFinisher() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        netCheck=new NetCheck();
        iF = new IntentFilter();
        iF.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iF.addAction(Intent.ACTION_TIME_TICK);
        iF.addAction(Intent.ACTION_TIME_CHANGED);
        iF.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netCheck);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(netCheck,iF);
        return START_STICKY;
    }

    class NetCheck extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();

            if (info != null && info.isConnected()) {
                Log.d(TAG, "onReceive: connected");
                Log.d(TAG, "onReceive: "+context.getApplicationInfo().className);
                new SendThisMessage().execute();
            }
        }
    }

    class SendThisMessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = DBHelper.openWritableDatabase(ScheduleFinisher.this);
            String[] projection = {
                    AwaitList.Columns.TO,
                    AwaitList.Columns.MSG,
                    AwaitList.Columns.CHAT_ID,
                    AwaitList.Columns.ID
            };
            Cursor c = db.query(
                    AwaitList.TABLE_NAME,
                    projection,
                    AwaitList.Columns.TIME_STAMP+"<=?",
                    new String[]{String.valueOf(System.currentTimeMillis())},
                    null,
                    null,
                    AwaitList.Columns.TIME_STAMP + " ASC"
            );
            while (c.moveToNext()) {
                String toPh = c.getString(c.getColumnIndex(AwaitList.Columns.TO));
                String msg = c.getString(c.getColumnIndex(AwaitList.Columns.MSG));
                int chId = c.getInt(c.getColumnIndex(AwaitList.Columns.CHAT_ID));
                int mID = c.getInt(c.getColumnIndex(AwaitList.Columns.ID));
                sendMessage(msg,getSender(),toPh,chId);
                int r=db.delete(AwaitList.TABLE_NAME, AwaitList.Columns.ID+"=?",new String[]{String.valueOf(mID)});
                Log.d(TAG, "doInBackground: rows affected"+r);
            }
            c.close();
            return null;
        }

        public String getSender() {
            SharedPreferences p=getSharedPreferences(FcmId.FCM_TOKEN_SAVE,Context.MODE_PRIVATE);
            return p.getString(LoginActivity.USER_PHONE,"");
        }

        public void sendMessage(final String message, final String senderNum, final String receiverNum, int chatID) {

            String url = URLEndPoints.EndPoints.NEW_MESSAGE.replace("_CHAT_ID_", String.valueOf(chatID));
            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: "+response);

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: "+error);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> postBody = new HashMap<>();
                    postBody.put("sender", senderNum);
                    postBody.put("receiver", receiverNum);
                    postBody.put("message", message);
                    return postBody;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }
            };
            Volley.newRequestQueue(getApplicationContext())
                    .add(request)
                    .setRetryPolicy(
                            new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2
                                    , DefaultRetryPolicy.DEFAULT_MAX_RETRIES
                                    , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                    );

        }
    }
}
