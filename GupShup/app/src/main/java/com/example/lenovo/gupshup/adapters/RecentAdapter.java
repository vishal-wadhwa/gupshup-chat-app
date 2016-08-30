package com.example.lenovo.gupshup.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lenovo.gupshup.DBHelper;
import com.example.lenovo.gupshup.Model.RecentChatsTable;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.activities.ChatScreen;
import com.example.lenovo.gupshup.db.ChatListRecord;

import java.util.ArrayList;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {
        private final TextView tvName;
        private RecentChatsTable singleChatObject;
        private static final int DEL_ITEM_ID = 9116;
        private View toRemoveView;
        public ViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.list_item);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select action:");
            toRemoveView=v;
            MenuItem item = menu.add(0, DEL_ITEM_ID,100,"Delete");
            item.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case DEL_ITEM_ID:
                    deleteRecord((Integer) toRemoveView.getTag());
                    return true;
                default:
                    return false;
            }
        }

        private void deleteRecord(int pos) {
            RecentChatsTable obj = chatList.remove(pos);
            SQLiteDatabase db = DBHelper.openWritableDatabase(context);
            db.delete(ChatListRecord.TABLE_NAME, ChatListRecord.Columns.PHONE+"=?",new String[] {obj.getPhone()});
            notifyItemRemoved(pos);
        }
    }

    private final ArrayList<RecentChatsTable> chatList;
    private Context context;

    public RecentAdapter(ArrayList<RecentChatsTable> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.singleChatObject = chatList.get(position);
        holder.tvName.setText(holder.singleChatObject.getName());
        holder.tvName.setTag(position);
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(holder.singleChatObject);
            }
        });

    }

    private void startChat(RecentChatsTable newChat) {
        Intent intent = new Intent(context, ChatScreen.class);
        intent.putExtra(ChatScreen.PERSON_NAME, newChat.getName());
        intent.putExtra(ChatScreen.PHONE_NUMBER, newChat.getPhone());
        intent.putExtra(ChatScreen.CHAT_ID, newChat.getChatId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
