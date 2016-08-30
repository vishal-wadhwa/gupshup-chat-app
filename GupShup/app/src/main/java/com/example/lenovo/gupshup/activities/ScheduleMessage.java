package com.example.lenovo.gupshup.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.lenovo.gupshup.DBHelper;
import com.example.lenovo.gupshup.Model.ScheduledMessages;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.ScheduleFinisher;
import com.example.lenovo.gupshup.db.AwaitList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleMessage extends AppCompatActivity implements View.OnClickListener {

    //TODO: filler and service with receivers
    private static final String TAG = "ScheduleMessage";
    private RecyclerView rView;
    private EditText etMsg;
    private TextView tvName;
    private FloatingActionButton fab;
    private Button btnDate, btnTime;
    private ArrayList<ScheduledMessages> awaitList = new ArrayList<>();
    private SchAdapter myAdapter;
    private String stringDate, stringTime;
    private String to, toName;
    private int toChatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_message);

        ComponentName cmp=startService(new Intent(this, ScheduleFinisher.class));
        Log.d(TAG, "onCreate: "+cmp.getShortClassName());
        new GetScheduled().execute();
        Intent i = getIntent();
        to = i.getStringExtra(ChatScreen.PHONE_NUMBER);
        toName = i.getStringExtra(ChatScreen.PERSON_NAME);
        toChatId = i.getIntExtra(ChatScreen.CHAT_ID, -1);
        stringDate = "";
        stringTime = "";
        tvName = (TextView) findViewById(R.id.sch_rec);
        etMsg = (EditText) findViewById(R.id.sch_msg);
        rView = (RecyclerView) findViewById(R.id.scheduled_msgs);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        btnDate = (Button) findViewById(R.id.select_date);
        btnTime = (Button) findViewById(R.id.select_time);

        tvName.setText("To: " + toName);
        etMsg.clearFocus();
        myAdapter = new SchAdapter(awaitList);
        rView.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapseBar = (CollapsingToolbarLayout) findViewById(R.id.collapse_bar);
        collapseBar.setTitle("Schedule");
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int pos = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSwiped: "+viewHolder.getAdapterPosition());
                SQLiteDatabase db = DBHelper.openWritableDatabase(ScheduleMessage.this);
                db.delete(AwaitList.TABLE_NAME, AwaitList.Columns.TO + "=?", new String[]{
                        awaitList.get(pos).getReceiver()
                });
            }
        });
        helper.attachToRecyclerView(rView);
        rView.setAdapter(myAdapter);
        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        fab.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_date:
                showDatePicker();
                break;
            case R.id.select_time:
                showTimePicker();
                break;
            case R.id.fab:
                Log.d(TAG, "onClick: " + AwaitList.TABLE_CRT_CMD);
                scheduleNewMessage();
        }
    }

    private void scheduleNewMessage() {
        new NewItemInserted().execute();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        final int mMonth = c.get(Calendar.MONTH);
        final int mYear = c.get(Calendar.YEAR);
        DatePickerDialog dtPick = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(mYear, mMonth, mDay);
                        view.setMinDate(c.getTimeInMillis());
                        stringDate = (dayOfMonth) + "-" + (monthOfYear + 1) + "-" + (year);
                        btnDate.setText(stringDate);
                        stringDate = (year) + "-" + (monthOfYear + 1) + "-" + (dayOfMonth);
                    }
                }, mYear, mMonth, mDay);
        dtPick.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog tmPick = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay > 12) {
                            stringTime = (hourOfDay % 12) + ":" + minute + " PM";
                        } else if (hourOfDay == 12) {
                            stringTime = hourOfDay + ":" + minute + " PM";
                        } else {
                            if (hourOfDay == 0) stringTime = "12:" + minute + " AM";
                            else stringTime = hourOfDay + ":" + minute + " AM";
                        }
                        btnTime.setText(stringTime);
                        stringTime = (hourOfDay) + ":" + minute + ":00.0";
                    }
                }, mHour, mMinute, false);
        tmPick.show();
    }

    class SchAdapter extends RecyclerView.Adapter<SchAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            private CardView cardView;
            private Button btnDel, btnEdit;
            private TextView tvTO, tvMsg, tvDate;
            private ScheduledMessages sms;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cd_view);
                btnDel = (Button) itemView.findViewById(R.id.btn_del);
                btnEdit = (Button) itemView.findViewById(R.id.btn_edit);
                tvTO = (TextView) itemView.findViewById(R.id.tv_to);
                tvDate = (TextView) itemView.findViewById(R.id.tv_date);
                tvMsg = (TextView) itemView.findViewById(R.id.tv_msg);
            }
        }

        private ArrayList<ScheduledMessages> schMsg = new ArrayList<>();

        public SchAdapter(ArrayList<ScheduledMessages> schMsg) {
            this.schMsg = schMsg;
        }

        @Override
        public SchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.sch_msg_layout, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final SchAdapter.ViewHolder holder, final int position) {
            holder.sms = schMsg.get(position);
            holder.tvTO.setText("TO: " + holder.sms.getReceiverName());
            holder.tvDate.setText("Send at: " + holder.sms.getTimeStamp());
            String txt = holder.sms.getMessage();
            if (txt.contains("\n")) txt = txt.split("\n")[0].concat("...");
            holder.tvMsg.setText("Message:\n" + txt);
            holder.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteDatabase db = DBHelper.openWritableDatabase(ScheduleMessage.this);
                    db.delete(AwaitList.TABLE_NAME, AwaitList.Columns.TO + "=?", new String[]{holder.sms.getReceiver()});
                    schMsg.remove(position);
                    notifyItemRemoved(position);
                }
            });
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder bld = new AlertDialog.Builder(ScheduleMessage.this);
                    bld.setTitle("MESSAGE");
                    bld.setMessage(holder.sms.getMessage());
                    bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    bld.create().show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return schMsg.size();
        }
    }

    class NewItemInserted extends AsyncTask<Void, Void, Void> {
        private String msg;
        private Date dt;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            msg = etMsg.getText().toString();
            if (msg.isEmpty()) {
                Toast.makeText(ScheduleMessage.this, "Message body empty", Toast.LENGTH_SHORT).show();
                cancel(true);
                return;
            }
            if (stringDate.isEmpty()) {
                Toast.makeText(ScheduleMessage.this, "Date not selected", Toast.LENGTH_SHORT).show();
                cancel(true);
                return;
            }
            if (stringTime.isEmpty()) {
                Toast.makeText(ScheduleMessage.this, "Time not set", Toast.LENGTH_SHORT).show();
                cancel(true);
                return;
            }
            String datetime = stringDate.concat(" ").concat(stringTime);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
            Log.d(TAG, "doInBackground: " + datetime);
            dt = new Date();
            try {
                dt = df.parse(datetime);
                Log.d(TAG, "doInBackground: " + dt.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground: " + dt.getTime());
            Date dt2 = new Date(System.currentTimeMillis());
            Log.d(TAG, "onPreExecute: " + dt2.toString());
            if (dt.before(dt2)) {
                Toast.makeText(ScheduleMessage.this, "Select date and time after current date", Toast.LENGTH_SHORT).show();
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = DBHelper.openWritableDatabase(ScheduleMessage.this);
            ContentValues values = new ContentValues();
            values.put(AwaitList.Columns.TO_NAME, toName);
            values.put(AwaitList.Columns.TO, to);
            values.put(AwaitList.Columns.CHAT_ID, toChatId);
            values.put(AwaitList.Columns.MSG, msg);
            values.put(AwaitList.Columns.TIME_STAMP, dt.getTime());
            db.insert(AwaitList.TABLE_NAME, null, values);
            stringTime = "";
            stringDate = "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            String dt2;
            dt2 = sdf.format(dt);
            Log.d(TAG, "doInBackground: " + dt2);
            ScheduledMessages schM = new ScheduledMessages(toName, to, msg, dt2);
            awaitList.add(schM);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            etMsg.setText("");
            btnDate.setText("DATE");
            btnTime.setText("TIME");
            myAdapter.notifyItemInserted(awaitList.size() - 1);
            rView.post(new Runnable() {
                @Override
                public void run() {
                    rView.smoothScrollToPosition(awaitList.size() - 1);
                }
            });
        }
    }

    class GetScheduled extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getSchedules();
            return null;
        }
    }

    private void getSchedules() {
        SQLiteDatabase db=DBHelper.openWritableDatabase(ScheduleMessage.this);
        String[] projection = {
                AwaitList.Columns.TO_NAME,
                AwaitList.Columns.TO,
                AwaitList.Columns.TIME_STAMP,
                AwaitList.Columns.MSG
        };
        Cursor c = db.query(
          AwaitList.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                AwaitList.Columns.TIME_STAMP+" ASC"
        );
        awaitList.clear();
        while(c.moveToNext()) {
            String toPh = c.getString(c.getColumnIndex(AwaitList.Columns.TO));
            String toN = c.getString(c.getColumnIndex(AwaitList.Columns.TO_NAME));
            String msg = c.getString(c.getColumnIndex(AwaitList.Columns.MSG));
            long time = c.getLong(c.getColumnIndex(AwaitList.Columns.TIME_STAMP));
            Date d = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            String dt= sdf.format(d);
            ScheduledMessages sMsgs = new ScheduledMessages(toN,toPh,msg,dt);
            awaitList.add(sMsgs);
        }
        c.close();
    }
}
