package com.example.lenovo.gupshup.activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.Model.ChatMessages;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.firebase.MessageService;
import com.example.lenovo.gupshup.firebase.URLEndPoints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatScreen extends AppCompatActivity {

    private static final String TAG = "ChatScreen";
    public static final String PERSON_NAME = "PSNAM";
    public static final String CHAT_ID = "chat_id";
    public static final String PHONE_NUMBER = "phone_number";
    private String receiverName;
    private int chatID;
    private String receiverNum;
    private String senderNum;

    private ArrayList<ChatMessages> msgs = new ArrayList<>();
    private EditText etChat;
    private ImageButton btnSend;
    private RecyclerView rView;
    private ChatMsgAdapter msgAdapter;
    private boolean isChatScreenOn = false;
    private static ChatScreen chscreen = null;
    private LinearLayoutManager manager;
    private BroadcastReceiver receiver;

    public static ChatScreen getInstance() {
        return chscreen;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll().penaltyLog().build()
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        NotificationManager managerN= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        managerN.cancelAll();
        getWindow().setBackgroundDrawableResource(R.drawable.wallpaper);
        chscreen = this;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ChatMessages chat = (ChatMessages) intent.getSerializableExtra(MessageService.MSG_TO_UPDATE);
                messageReceived(chat);
            }
        };

        Intent intent = getIntent();
        receiverName = intent.getStringExtra(PERSON_NAME);
        chatID = intent.getIntExtra(CHAT_ID, -1);
        receiverNum = intent.getStringExtra(PHONE_NUMBER);
        senderNum = getSharedPreferences(FcmId.FCM_TOKEN_SAVE, MODE_PRIVATE)
                .getString(LoginActivity.USER_PHONE, null);


        setTitle(receiverName);

        msgFiller();

        etChat = (EditText) findViewById(R.id.chat_box);
        etChat.requestFocus();
        btnSend = (ImageButton) findViewById(R.id.btn_send);
        rView = (RecyclerView) findViewById(R.id.msg_list);
        manager = new LinearLayoutManager(this);
        rView.setLayoutManager(manager);
        msgAdapter = new ChatMsgAdapter(msgs);
        rView.setAdapter(msgAdapter);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etChat.getText().toString().isEmpty()) {
                    sendMessage(etChat.getText().toString());
                    etChat.setText("");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_screen_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.schedule_msg:
                Intent intent = new Intent(this, ScheduleMessage.class);
                intent.putExtra(PERSON_NAME,receiverName);
                intent.putExtra(PHONE_NUMBER,receiverNum);
                intent.putExtra(CHAT_ID,chatID);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void messageReceived(ChatMessages msg) {
        msgs.add(msg);
        msgAdapter.notifyDataSetChanged();
        rView.post(new Runnable() {
            @Override
            public void run() {
                rView.smoothScrollToPosition(msgs.size());
            }
        });

    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private TextView tvMsg;
        private TextView tvTime;
        private ChatMessages messages;

        public MyHolder(View itemView) {
            super(itemView);
            tvMsg = (TextView) itemView.findViewById(R.id.msg);
            tvTime = (TextView) itemView.findViewById(R.id.time_stamp);

        }
    }

    class ChatMsgAdapter extends RecyclerView.Adapter<MyHolder> {

        private ArrayList<ChatMessages> msgData = new ArrayList<>();

        public ChatMsgAdapter(ArrayList<ChatMessages> msgData) {
            this.msgData = msgData;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(viewType, parent, false);
            return new MyHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (senderNum.equals(msgData.get(position).getSender())) {
                return R.layout.message_box_sent;
            } else return R.layout.message_box_receive;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.messages = msgData.get(position);
            holder.tvMsg.setText(holder.messages.getMessage());
            holder.tvTime.setText(holder.messages.getTime());
        }

        @Override
        public int getItemCount() {
            return msgData.size();
        }
    }

    public void sendMessage(final String message) {

        String url = URLEndPoints.EndPoints.NEW_MESSAGE.replace("_CHAT_ID_", String.valueOf(chatID));
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject temp = new JSONObject(response);
                            JSONObject temp2 = temp.getJSONObject("message");
                            Log.d(TAG, "onResponse: " + temp2.toString());
                            String time = temp2.getString("sending_time");
                            int id = temp2.getInt("message_id");
                            ChatMessages cmsgs = new ChatMessages(id, message, time, senderNum);
                            msgs.add(cmsgs);
                            msgAdapter.notifyDataSetChanged();
                            rView.post(new Runnable() {
                                @Override
                                public void run() {
                                    rView.smoothScrollToPosition(msgs.size());
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError)
                            Toast.makeText(ChatScreen.this, "Connection Timed Out. Please retry", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postBody = new HashMap<>();
                postBody.put("sender", senderNum);
                postBody.put("receiver", receiverNum);
                postBody.put("message", message);

                Log.d(TAG, "Map: " + postBody);
                return postBody;
            }

            @Override
            public String getBodyContentType() {
                Log.d(TAG, "ContentBodyTypeloginact");
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

    public void msgFiller() {
        msgs.clear();
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URLEndPoints.EndPoints.GET_CHATS + chatID,
                new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "onResponse: loading");
                        new MessageLoad().execute(response);
                        Log.d(TAG, "onResponse: " + msgs);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(ChatScreen.this, "Connection Timed Out", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    public boolean isAppForeground(int chatId) {
        return isChatScreenOn && chatId==chatID;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        isChatScreenOn = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(MessageService.MSG_RECEIVED));
        isChatScreenOn = true;
    }

    class MessageLoad extends AsyncTask<JSONArray, Void, Void> {

        @Override
        protected Void doInBackground(JSONArray... params) {

            for (int i = 0; i < params[0].length(); i++) {
                try {
                    JSONObject json = params[0].getJSONObject(i);
                    ChatMessages messages = new ChatMessages(
                            json.getInt("message_id"),
                            json.getString("message"),
                            json.getString("sending_time"),
                            json.getString("sender_phone")
                    );
                    msgs.add(messages);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            msgAdapter.notifyDataSetChanged();
            rView.post(new Runnable() {
                @Override
                public void run() {
                    rView.smoothScrollToPosition(msgs.size());
                }
            });

        }
    }
}
