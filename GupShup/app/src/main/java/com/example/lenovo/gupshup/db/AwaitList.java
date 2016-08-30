package com.example.lenovo.gupshup.db;

/**
 * Created by Lenovo on 20-Aug-16.
 */
public class AwaitList extends SQLQueryCommands {
    public static final String TABLE_NAME = "AwaitList";

    public interface Columns {
        String ID = "schedule_id";
        String CHAT_ID = "chat_id";
        String TO = "to_ph";
        String TIME_STAMP = "time_stamp";
        String MSG = "message";
        String TO_NAME = "name";
    }

    public static final String TABLE_CRT_CMD =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                    + LBR
                    + Columns.ID + TYPE_INT_PK + COMMA
                    + Columns.CHAT_ID + TYPE_INT + COMMA
                    + Columns.TO + TYPE_TEXT + COMMA
                    + Columns.TO_NAME + TYPE_TEXT + COMMA
                    + Columns.TIME_STAMP + TYPE_INT + COMMA
                    + Columns.MSG + TYPE_TEXT
                    + RBR + SEMI_COLON;

}
