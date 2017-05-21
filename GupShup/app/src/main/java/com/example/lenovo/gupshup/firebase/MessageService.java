package com.example.lenovo.gupshup.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.lenovo.gupshup.DBHelper;
import com.example.lenovo.gupshup.Model.ChatMessages;
import com.example.lenovo.gupshup.Model.RecentChatsTable;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.activities.ChatScreen;
import com.example.lenovo.gupshup.activities.MainActivity;
import com.example.lenovo.gupshup.db.ChatListRecord;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Lenovo on 12-Aug-16.
 */
public class MessageService extends FirebaseMessagingService {

    //TODO: Collapsible notification
    private static final String TAG = "MessageService";
    private LocalBroadcastManager manager;
    public static final String MSG_RECEIVED = "MY_MSG_RECEIVE_INTENT";
    public static final String  MSG_TO_UPDATE = "NEW_MSG";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            MediaPlayer player = MediaPlayer.create(this, R.raw.whistle);
            player.start();
            Map<String, String> data = remoteMessage.getData();

            Iterator<String> keyset = data.keySet().iterator();
            String key;
            while ((key = keyset.next()) != null) {
                Log.d(TAG, "onMessageReceived: " + key);
            }
            try {
                JSONObject jsonObject = new JSONObject(data.get("data"));
                JSONObject message = jsonObject.getJSONObject("message");

                ChatMessages cMsgs = new ChatMessages(
                        message.getInt("message_id"),
                        message.getString("message"),
                        message.getString("sending_time"),
                        message.getString("sender_phone")
                );
                int chatId = message.getInt("chat_id");
                ChatScreen screen = ChatScreen.getInstance();
                if (screen != null && screen.isAppForeground(chatId)) triggerListUpdate(cMsgs);
                else {

                    Intent intent = new Intent(this, ChatScreen.class);
                    intent.putExtra(ChatScreen.PERSON_NAME, data.get("title"));
                    intent.putExtra(ChatScreen.PHONE_NUMBER, cMsgs.getSender());
                    intent.putExtra(ChatScreen.CHAT_ID, chatId);

                    RecentChatsTable chat = new RecentChatsTable(data.get("title"), chatId, cMsgs.getSender());
                    updateDB(chat);
                    showNotif(data.get("title"),cMsgs.getMessage(),intent);
//                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 901, intent, 0);
//                    pi.send();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNotif(String title, String message, Intent intent) {
        if (message.isEmpty()) return;

        int icon = R.drawable.app_icon;

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        Notification notification;
        notification = builder.setAutoCancel(true)
                .setSmallIcon(icon)
                .setStyle(inboxStyle)
                .setColor(Color.BLUE)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setWhen(SystemClock.uptimeMillis())
                .build();

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(290,notification);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = LocalBroadcastManager.getInstance(getApplicationContext());

    }

    private void updateDB(RecentChatsTable object) {
        SQLiteDatabase database = DBHelper.openWritableDatabase(getApplicationContext());
        String[] projection = {
                ChatListRecord.Columns.NAME,
                ChatListRecord.Columns.PHONE,
                ChatListRecord.Columns.CHAT_ID
        };
        Cursor c = database.query(
                ChatListRecord.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                ChatListRecord.Columns.ID + " DESC "
        );
        while (c.moveToNext()) {
            RecentChatsTable chatsTable = new RecentChatsTable(
                    c.getString(c.getColumnIndex(ChatListRecord.Columns.NAME)),
                    c.getInt(c.getColumnIndex(ChatListRecord.Columns.CHAT_ID)),
                    c.getString(c.getColumnIndex(ChatListRecord.Columns.PHONE))
            );
            if (object.getPhone().equals(chatsTable.getPhone())) {
                c.close();
                return;
            }
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(ChatListRecord.Columns.NAME, object.getName());
        values.put(ChatListRecord.Columns.CHAT_ID, object.getChatId());
        values.put(ChatListRecord.Columns.PHONE, object.getPhone());
        database.insert(ChatListRecord.TABLE_NAME, null, values);
    }

    private void triggerListUpdate(ChatMessages object) {
        Intent intent = new Intent(MSG_RECEIVED);
        if (object!=null) intent.putExtra(MSG_TO_UPDATE,object);
        manager.sendBroadcast(intent);
    }
}
