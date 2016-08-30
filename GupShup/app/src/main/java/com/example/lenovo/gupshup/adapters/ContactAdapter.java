package com.example.lenovo.gupshup.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.Model.ContactData;
import com.example.lenovo.gupshup.Model.RecentChatsTable;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.activities.LoginActivity;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.firebase.URLEndPoints;
import com.example.lenovo.gupshup.fragments.RecentChats;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements INameableAdapter{

    private static final String TAG = "ContactAdapter";
    private final ArrayList<ContactData> contactList;
    private Context context;
    RecentChatsTable person;

    public ContactAdapter(ArrayList<ContactData> contactList, Context context) {
        this.contactList = contactList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.ctData = contactList.get(position);
        holder.ctName.setText(holder.ctData.getName());
        holder.ctPhone.setText(holder.ctData.getPhone());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUserPresent(holder.ctData.getName(), holder.ctData.getPhone());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public Character getCharacterForElement(int element) {
        Log.d(TAG, "getCharacterForElement: "+element);
        return contactList.get(element).getName().charAt(0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private TextView ctName;
        private TextView ctPhone;
        private ContactData ctData;

        public ViewHolder(View view) {
            super(view);
            layout = (LinearLayout) view.findViewById(R.id.content);
            ctName = (TextView) view.findViewById(R.id.name_ct);
            ctPhone = (TextView) view.findViewById(R.id.phone_ct);
        }
    }

    private void isUserPresent(final String name, String phone) {

        phone = phone.replace("+91", "").replace(" ", "");
        if (phone.charAt(0) == '0') phone = phone.substring(1, phone.length());
        Log.d(TAG, "isUserPresent: " + URLEndPoints.EndPoints.GET_USER + phone);
        final String finalPhone = phone;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URLEndPoints.EndPoints.GET_USER + phone,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean isPresent = response.getJSONObject("user").getString("phone_no").length() >= 10;
                            Log.d(TAG, "onResponse: " + response);
                            if (isPresent) getChatByPhone(name, finalPhone);
                            else Toast.makeText(context, "User not on gupshup", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show();
                    }
                });
        Volley.newRequestQueue(context).add(req);
    }

    private void getChatByPhone(final String receiverName, String receiver) {


        String sender = getSender();
        sender = sender.replace("+91", "").replace(" ", "");

        String url = URLEndPoints.EndPoints
                .GET_CHAT_ID.replace("_send_", sender)
                .replace("_receive_", receiver);

        Log.d(TAG, "getChatByPhone: " + url);
        final String finalReceiver = receiver;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int chatId = response.getJSONObject("user").getInt("chat_id");
                            person = new RecentChatsTable(receiverName, chatId, finalReceiver);
                            RecentChats.getInstance().updateDB(person);
                            Log.d(TAG, "onResponse: " + person);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        Volley.newRequestQueue(context).add(req);
        Log.d(TAG, "getChatByPhone: " + person);
    }

    private String getSender() {
        SharedPreferences pref = context.getSharedPreferences(FcmId.FCM_TOKEN_SAVE, Context.MODE_PRIVATE);
        return pref.getString(LoginActivity.USER_PHONE, null);
    }


}
