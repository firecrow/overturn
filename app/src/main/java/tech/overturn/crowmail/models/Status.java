package tech.overturn.crowmail.models;

import tech.overturn.crowmail.CrowNotification;
import tech.overturn.crowmail.CrowmailException;
import tech.overturn.crowmail.Global;
import tech.overturn.crowmail.R;
import android.app.Notification;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.solver.Goal;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import tech.overturn.crowmail.Data;
import tech.overturn.crowmail.Orm;


public class Status extends Data {
    public static String tableName  = "status";
    public Integer _id;
    public Integer account_id;
    public Date date;
    public String type;
    public String description;
    public Long longval;
    public String textval;

    public static String ERROR_TYPE = "error";
    public static String INFO_TYPE = "info";
    public static String LATEST_FETCH_TYPE = "latest_fetch";

    public void log(SQLiteDatabase db) {
        Log.d("fcrow", toString());
        Orm.insert(db, Status.tableName, this);
    }

    public String toString() {
        return String.format("<Status %s -> %s/%d>", type, textval, longval);
    }

    public static void fromCme(Context context, SQLiteDatabase db, String label, CrowmailException cme, boolean notify) {
        Status s = new Status();
        s.textval = label+':'+cme.getClass().getName();
        Throwable cause = cme;
        while(cause.getCause() != null) {
            cause = cause.getCause();
        }
        s.account_id = cme.a.data._id;
        s.date = new Date();
        s.description = stackToString(cme);
        s.type = ERROR_TYPE;
        s.log(db);
        if(notify) {
            new CrowNotification(context).send(s.type, s.textval, Global.CROWMAIL_ERROR+' '+cme.a.data._id, R.drawable.exc, false);
        }
    }

    public static void fromStrings(Context context, SQLiteDatabase db, String type, Integer account_id, String label, String description, boolean notify) {
        Status s = new Status();
        s.type = type;
        s.textval = label;
        s.description = description;
        s.account_id = account_id;
        s.date = new Date();
        s.log(db);
        if(notify) {
            new CrowNotification(context).send(s.type, s.textval, Global.CROWMAIL_ERROR+' '+account_id, R.drawable.exc, false);
        }
    }

    private static String stackToString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
