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


public class ErrorStatus extends Data {
    public static String tableName  = "errorstatus";
    public static Integer notify_offset = 2;
    public Integer date;
    public Integer _id;
    public String key;
    public String message;
    public String cause;
    public String name;
    public String location;
    public Integer message_id;
    public Integer account_id;
    public String stack;

    public void log(SQLiteDatabase db){
        Orm.insert(db, ErrorStatus.tableName, this);
    }

    public static void fromCme(Context context, SQLiteDatabase db, String location, CrowmailException cme, boolean notify) {
        ErrorStatus s = new ErrorStatus();
        Log.d("fcrow", "------- Error from cme" + cme.getMessage(), cme);
        s.location = location;
        Log.d("fcrow", "------- 1");
        s.location = location;
        s.key = cme.key.toString();
        Log.d("fcrow", "------- 2");
        s.name = cme.getClass().getSimpleName();
        Log.d("fcrow", "------- 3");
        s.name = cme.getClass().getSimpleName();
        Log.d("fcrow", "------- 4");
        s.message = cme.getMessage();
        Log.d("fcrow", "------- 5");
        Exception e = cme;
        Throwable cause = null;
        Log.d("fcrow", "------- 6");
        cause = e;
        while(cause.getCause() != null) {
            cause = cause.getCause();
        }
        Log.d("fcrow", "------- 7");
        if(cause != null) {
            s.cause = cme.getCause().getClass().getSimpleName();
        }
        Log.d("fcrow", "------- 8");
        s.account_id = cme.a.data._id;
        Log.d("fcrow", "------- 8");
        s.stack = stackToString(cme);
        Log.d("fcrow", "------- 10");
        s.date = new Long(new Date().getTime()).intValue();
        Log.d("fcrow", "------- 11");
        s.log(db);
        Log.d("fcrow", String.format("------- notify is:%b", notify));
        if(notify) {
            Log.d("fcrow", "------- 12");
            new CrowNotification(context).send(cme.key.toString(), +cme.a.data._id+' '+s.name+'<'+s.cause, Global.CROWMAIL_ERROR+' '+cme.a.data._id, R.drawable.exc, false);
        }
    }

    public static void fromStrings(Context context, SQLiteDatabase db, String location, String key, String message, Integer account_id, boolean notify) {
        ErrorStatus s = new ErrorStatus();
        s.location = location;
        s.key = key.toString();
        s.message = message;
        s.name = "FromString";
        s.account_id = account_id;
        s.date = new Long(new Date().getTime()).intValue();
        s.log(db);
        if(notify) {
            new CrowNotification(context).send(location+':'+key.toString(), message, Global.CROWMAIL_ERROR+' '+account_id, R.drawable.exc, false);
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
