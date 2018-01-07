package tech.overturn.crowmail.models;

import android.app.Notification;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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
    public Integer message_id;
    public Integer account_id;
    public String stack;

    public ErrorStatus log(SQLiteDatabase db, String key, String message, String cause, String stack, Integer account_id, Integer message_id) {
        this.key = key;
        this.message = message;
        this.cause = cause;
        this.stack = stack;
        this.account_id = account_id;
        this.message_id = message_id;
        this.date = new Long(new Date().getTime() / 1000).intValue();
        Orm.insert(db, ErrorStatus.tableName, this);
        return this;
    }

    public ErrorStatus log(SQLiteDatabase db, String key, String message, Integer account_id, Integer message_id) {
        this.key = key;
        this.message = message;
        this.account_id = account_id;
        this.message_id = message_id;
        this.date = new Long(new Date().getTime() / 1000).intValue();
        Orm.insert(db, ErrorStatus.tableName, this);
        return this;
    }

    public ErrorStatus log(SQLiteDatabase db, String key, String message) {
        this.key = key;
        this.message = message;
        this.date = new Long(new Date().getTime() / 1000).intValue();
        Orm.insert(db, ErrorStatus.tableName, this);
        return this;
    }

    public void sendNotify(Context context, Boolean vibrate) {
        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
        Notification sum = new Notification.Builder(context)
                .setSmallIcon(R.drawable.exc)
                .setGroupSummary(true)
                .setGroup("CROWMAIL_ERROR")
                .build()
                ;
        nmng.notify("CROWMAIL_ERROR", -1, sum);
        Notification.Builder nb = new Notification.Builder(context)
                .setContentTitle(key)
                .setContentText(message+cause)
                .setSmallIcon(R.drawable.exc)
                .setGroupSummary(false)
                .setGroup("CROWMAIL_ERROR")
                ;
        if (vibrate) {
            nb.setVibrate(new long[]{ 1000, 1000, 1000})
        }
        nmng.notify("CROWMAIL_ERROR", -(notify_offset++), nb.build());
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
