package tech.overturn.crowmail.models;

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

    public static void fromCme(Context context, SQLiteDatabase db, String location, CrowmailException cme) {
        ErrorStatus s = new ErrorStatus();
        if(cme.key.equals(CrowmailException.CONNECTION)
                || cme.key.equals(CrowmailException.ERROR)
                || cme.key.equals(CrowmailException.TIMEOUT)
                || cme.key.equals(CrowmailException.UNKNOWN)){
            s.sendNotify(context, false);
        }
        Log.d("fcrow", "------- Error in Fetcher loop " + cme.getMessage(), cme);
        s.key = cme.key;
        s.location = location;
        s.name = cme.getClass().getSimpleName();
        s.message = cme.getMessage();
        if (cme.getCause() != null) {
            s.cause = cme.getClass().getSimpleName();
        }
        s.account_id = cme.a.data._id;
        s.stack = stackToString(cme);
        s.log(db);
        s.sendNotify(context, false);
    }

    public static String stackToString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public void sendNotify(Context context, Boolean vibrate) {
        String msg_group_key;
        if (account_id != null) {
            msg_group_key = String.format("%s%d", Global.CROWMAIL_ERROR, account_id);
        } else {
            msg_group_key = Global.CROWMAIL_ERROR;
        }
        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
        Notification sum = new Notification.Builder(context)
                .setSmallIcon(R.drawable.exc)
                .setGroupSummary(true)
                .setGroup(msg_group_key)
                .build()
                ;
        nmng.notify(msg_group_key, -1, sum);
        String key_str = this.key;
        if(account_id != null) {
            key_str += " ac:"+account_id.toString();
        }
        Notification.Builder nb = new Notification.Builder(context)
                .setContentTitle(key_str)
                .setContentText(cause+" "+message)
                .setSmallIcon(R.drawable.exc)
                .setGroupSummary(false)
                .setGroup(msg_group_key)
                ;
        if (vibrate) {
            nb.setVibrate(new long[]{ 1000, 1000, 1000});
        }
        nmng.notify(msg_group_key, -(notify_offset++), nb.build());
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
