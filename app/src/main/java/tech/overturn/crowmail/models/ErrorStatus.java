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
        s.key = cme.key.toString();
        s.name = cme.getClass().getSimpleName();
        s.message = cme.getMessage();
        if (cme.getCause() != null) {
            s.cause = cme.getClass().getSimpleName();
        }
        s.account_id = cme.a.data._id;
        s.stack = stackToString(cme);
        s.log(db);
        if(notify) {
            new CrowNotification(context).send(cme.key.toString(), s.name+'<'+s.cause, Global.CROWMAIL_ERROR+' '+cme.a.data._id, R.drawable.exc, false);
        }
    }

    public static void fromStrings(Context context, SQLiteDatabase db, String location, String key, String message, Integer account_id, boolean notify) {
        ErrorStatus s = new ErrorStatus();
        s.location = location;
        s.key = key.toString();
        s.message = message;
        s.name = "FromString";
        s.account_id = account_id;
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
