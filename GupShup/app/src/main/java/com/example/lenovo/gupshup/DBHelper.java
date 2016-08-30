package com.example.lenovo.gupshup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.lenovo.gupshup.db.AwaitList;
import com.example.lenovo.gupshup.db.ChatListRecord;

/**
 * Created by Lenovo on 29-Jul-16.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    public static final String DB_NAME = "Chat";
    public static final int DB_VER = 1;
    private static DBHelper dbHelper = null;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public static SQLiteDatabase openWritableDatabase(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
            Log.d(TAG, "New DB object");
        }
        return dbHelper.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ChatListRecord.TABLE_CREATE_CMD);
        Log.d(TAG, "onCreate: "+ChatListRecord.TABLE_CREATE_CMD);
        //db.execSQL("DROP TABLE Await_List;");
        db.execSQL(AwaitList.TABLE_CRT_CMD);
        Log.d(TAG, "onCreate: "+AwaitList.TABLE_CRT_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
