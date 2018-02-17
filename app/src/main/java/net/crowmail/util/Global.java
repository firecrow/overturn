package net.crowmail.util;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import net.crowmail.model.Ledger;

public class Global {
    public static final String CROWMAIL = "crowmail";
    public static final String CROWMAIL_ERROR = "crowmail_error";
    public static final String TRIGGER_SEND = "net.crowmail.TRIGGER_SEND";
    public static final String SEND_ACTION = "net.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "net.crowmail.SEND_STATUS";
    public static final String TRIGGER_FETCH = "net.crowmail.TRIGGER_FETCH";
    public static final String COMPLETE = "complete";
    public static final String GLOBAL_BROADCAST = "net.crowmail.GLOBAL_BROADCAST";
    public static final String START_SERVICE = "net.crowmail.START_SERVICE";
    public static final String NETWORK_STATUS = "net.crowmail.NETWORK_STATUS";

    public static DBHelper dbh;

    private static DBHelper getDbh(Context context) {
        if(dbh == null) {
            dbh = new DBHelper(context);
        }
        return dbh;
    }

    public static SQLiteDatabase getWriteDb(Context context) {
        return getDbh(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadDb(Context context) {
        return getDbh(context).getReadableDatabase();
    }

    public static String stackToString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}