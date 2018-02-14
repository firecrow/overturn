package tech.overturn.crowmail;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tech.overturn.crowmail.models.Ledger;

public class Global {
    public static final String CROWMAIL = "crowmail";
    public static final String CROWMAIL_ERROR = "crowmail_error";
    public static final String TRIGGER_SEND = "tech.overturn.crowmail.TRIGGER_SEND";
    public static final String SEND_ACTION = "tech.overturn.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.crowmail.SEND_STATUS";
    public static final String TRIGGER_FETCH = "tech.overturn.crowmail.TRIGGER_FETCH";
    public static final String COMPLETE = "complete";
    public static final String GLOBAL_BROADCAST = "tech.overturn.crowmail.GLOBAL_BROADCAST";
    public static final String START_SERVICE = "tech.overturn.crowmail.START_SERVICE";
    public static final String NETWORK_STATUS = "tech.overturn.crowmail.NETWORK_STATUS";

    public static boolean networkUp = false;
    public static DBHelper dbh;

    public static List<Runnable> onNetworkUpTrue = new ArrayList<Runnable>();

    public static void setNetworkUp(Context context, boolean up) {
        if (networkUp == up) {
            return;
        }
        Ledger.fromStrings(context, getWriteDb(context),
                Ledger.NETWORK_STATUS_TYPE,
                null,
                String.format("network is up:%b", up),
                null,
                false);
        if (up) {
            for(Runnable runner: onNetworkUpTrue) {
                runner.run();
            }
            onNetworkUpTrue = new ArrayList<Runnable>();
        }
        networkUp = up;
    }

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
}
