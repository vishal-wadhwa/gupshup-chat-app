package com.example.lenovo.gupshup.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.DBHelper;
import com.example.lenovo.gupshup.Model.RecentChatsTable;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.RecyclerViewDecoration;
import com.example.lenovo.gupshup.activities.AboutUs;
import com.example.lenovo.gupshup.activities.ChatScreen;
import com.example.lenovo.gupshup.activities.LoginActivity;
import com.example.lenovo.gupshup.activities.MainActivity;
import com.example.lenovo.gupshup.adapters.RecentAdapter;
import com.example.lenovo.gupshup.db.ChatListRecord;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.firebase.URLEndPoints;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RecentChats extends Fragment implements MainActivity.OnBackPressedListener {

    // TODO: Customize parameter argument names
    private static final String TAG = "RecentChats";
    private RecyclerView recyclerView;
    private RecentAdapter adapter;
    private ArrayList<RecentChatsTable> list = new ArrayList<>();
    private static RecentChats chats = null;
    private SearchView.OnQueryTextListener listener;
    private MenuItem searchItem;
    private SearchView searchView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecentChats() {
    }

    // TODO: Customize parameter initialization
    public static RecentChats getInstance() {
        if (chats == null) {
            chats = new RecentChats();
        }
        return chats;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setBackListenerForFrag1(this);
        setHasOptionsMenu(true);
        adapter = new RecentAdapter(list, getActivity());
        new FillChatList().execute();
        Log.d(TAG, "oncreate of recents");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: recents");
        View view = inflater.inflate(R.layout.recent_list_layout, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            Drawable divider = ContextCompat.getDrawable(context, R.drawable.my_divider);
            recyclerView.addItemDecoration(new RecyclerViewDecoration(divider));
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        searchItem = menu.findItem(R.id.srch_actn);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setInputType(InputType.TYPE_CLASS_PHONE);
        searchView.setQueryHint("Not in contacts? Enter number");
        listener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                existenceCheck(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(listener);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        searchView.setOnQueryTextListener(listener);
        switch (item.getItemId()) {
            case R.id.srch_actn:
                return true;
            case R.id.about:
                startActivity(new Intent(getActivity(), AboutUs.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean doBack() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.onActionViewCollapsed();
            return true;
        }
        return false;
    }

    private void fillTheList() {
        SQLiteDatabase database = DBHelper.openWritableDatabase(getActivity());
        String[] projection = {
                ChatListRecord.Columns.ID,
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
        list.clear();
        while (c.moveToNext()) {
            RecentChatsTable chatsTable = new RecentChatsTable(
                    c.getString(c.getColumnIndex(ChatListRecord.Columns.NAME)),
                    c.getInt(c.getColumnIndex(ChatListRecord.Columns.CHAT_ID)),
                    c.getString(c.getColumnIndex(ChatListRecord.Columns.PHONE))
            );
            list.add(chatsTable);
        }
        c.close();
    }

    class FillChatList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            fillTheList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }

    public void refresh(RecentChatsTable object, boolean isExisting) {
        if (isExisting) {
            list.add(object);
            adapter.notifyDataSetChanged();
        }
        Intent intent = new Intent(getActivity(), ChatScreen.class);
        intent.putExtra(ChatScreen.PERSON_NAME, object.getName());
        intent.putExtra(ChatScreen.PHONE_NUMBER, object.getPhone());
        intent.putExtra(ChatScreen.CHAT_ID, object.getChatId());
        startActivity(intent);
    }

    private boolean containsNumber(String phone) {
        for (RecentChatsTable oj :
                list) {
            if (oj.getPhone().equals(phone)) return true;
        }
        return false;
    }

    //TODO: check for user existence before sending volley request. Hence, reducing network call.

    public void updateDB(RecentChatsTable object) {
        if (object != null) {
            if (!containsNumber(object.getPhone())) {
                SQLiteDatabase database = DBHelper.openWritableDatabase(getActivity());
                ContentValues values = new ContentValues();
                values.put(ChatListRecord.Columns.NAME, object.getName());
                values.put(ChatListRecord.Columns.CHAT_ID, object.getChatId());
                values.put(ChatListRecord.Columns.PHONE, object.getPhone());
                database.insert(ChatListRecord.TABLE_NAME, null, values);
                refresh(object, false);
            } else refresh(object, true);
        }
    }

    private void existenceCheck(String num) {
        num = num.replace(" ", "").replace("+91", "");
        if (num.charAt(0) == '0') num = num.substring(1, num.length());

        final String finalNum = num;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URLEndPoints.EndPoints.GET_USER + num,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jObj = response.getJSONObject("user");
                            boolean isPresent = jObj.getString("phone_no").length() >= 10;
                            if (!isPresent)
                                Toast.makeText(getActivity(), "User not on Gupshup", Toast.LENGTH_SHORT).show();
                            else loadUser(finalNum, jObj.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: ");
                    }
                });
        Volley.newRequestQueue(getActivity()).add(req);

    }

    private void loadUser(String ph, final String phName) {
        String requester = getActivity().getSharedPreferences(FcmId.FCM_TOKEN_SAVE, Context.MODE_PRIVATE)
                .getString(LoginActivity.USER_PHONE, "");
        requester = requester.replace("+91", "").replace(" ", "");
        if (requester.charAt(0) == '0') requester = requester.substring(1, requester.length());

        String url = URLEndPoints.EndPoints
                .GET_CHAT_ID.replace("_send_", requester)
                .replace("_receive_", ph);

        final String finalReceiver = ph;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int chatId = response.getJSONObject("user").getInt("chat_id");
                            RecentChatsTable person = new RecentChatsTable(phName, chatId, finalReceiver);
                            updateDB(person);
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
        Volley.newRequestQueue(getActivity()).add(req);
    }
}
