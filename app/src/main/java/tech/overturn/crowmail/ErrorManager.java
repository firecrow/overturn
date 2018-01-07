package tech.overturn.crowmail;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import tech.overturn.crowmail.models.ErrorStatus;

public class ErrorManager {
    DBHelper dbh;
    Context context;
    public static Integer notify_offset = 2;

    public ErrorManager(Context context, DBHelper dbh) {
        this.dbh = dbh;
        this.context = context;
    }

    public void error(String key, String message, String cause, String stack, Integer account_id, Integer message_id) {
        ErrorStatus error = new ErrorStatus();
        error.key = key;
        error.message = message;
        error.cause = cause;
        error.stack = stack;
        error.account_id = account_id;
        error.message_id = message_id;
        error.date = new Long(new Date().getTime() / 1000).intValue();
        Orm.insert(dbh.getWritableDatabase(), ErrorStatus.tableName, error);
        sendNotify(key, error.message);
    }

    public void sendNotify(String title, String body) {
        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
        Notification sum = new Notification.Builder(context)
                .setSmallIcon(R.drawable.exc)
                .setGroupSummary(true)
                .setGroup("CROWMAIL_ERROR")
                .build();
        nmng.notify("CROWMAIL_ERROR", -1, sum);
        Notification n = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.exc)
                .setVibrate(new long[]{ 1000, 1000, 1000})
                .setGroupSummary(false)
                .setGroup("CROWMAIL_ERROR")
                .build();
        nmng.notify("CROWMAIL_ERROR", -(notify_offset++), n);
    }
}
