package tech.overturn.util;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tech.overturn.model.Ledger;

public class Global {
    public static final String GLOBAL = "global";
    public static final String CROWMAIL = "crowmail";
    public static final String CROWMAIL_ERROR = "crowmail_error";
    public static final String TRIGGER_SEND = "tech.overturn.TRIGGER_SEND";
    public static final String SEND_ACTION = "tech.overturn.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.SEND_STATUS";
    public static final String TRIGGER_FETCH = "tech.overturn.TRIGGER_FETCH";
    public static final String TRIGGER_STOP = "tech.overturn.TRIGGER_STOP";
    public static final String COMPLETE = "complete";
    public static final String GLOBAL_BROADCAST = "tech.overturn.GLOBAL_BROADCAST";
    public static final String START_SERVICE = "tech.overturn.START_SERVICE";
    public static final String NETWORK_STATUS = "tech.overturn.NETWORK_STATUS";

    public static DBHelper dbh;
    public static ConnectivityManager cm;
    public static PowerManager.WakeLock wake;


    private static DBHelper getDbh(Context context) {
        if(dbh == null) {
            dbh = new DBHelper(context);
        }
        return dbh;
    }

    public static SQLiteDatabase getDb(Context context) {
        return getDbh(context).getWritableDatabase();
    }

    public static String stackToString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static Boolean hasNetwork(Context context) {
        Boolean up = false;
        Network[] networks = getCm(context).getAllNetworks();
        for (Network n: networks) {
            NetworkInfo info = cm.getNetworkInfo(n);
            if(info != null && info.isConnected()){
                up = true;
            }
        }
        return up;
    }

    public static ConnectivityManager getCm(Context context) {
        if(cm == null){
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return cm;
    }
}
