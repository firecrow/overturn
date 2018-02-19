package tech.overturn.model;

import tech.overturn.util.DbField;
import tech.overturn.util.Orm;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Ledger extends Data {
    public static String tableName  = "ledger";

    @DbField
    public Long parent_id;

    @DbField
    public String entity;

    @DbField
    public Date date;

    @DbField
    public String type;

    @DbField
    public String description;

    @DbField
    public Long longval;

    @DbField
    public String textval;

    public static String ERROR_TYPE = "error";
    public static String NETWORK_STATUS_TYPE = "network_status";
    public static String NETWORK_UNREACHABLE = "network_unreachable";
    public static String MESSAGING_ERROR = "messaging_error";
    public static String INFO_TYPE = "info";
    public static String UID_NEXT = "uidnext";
    public static String MESSAGE_COUNT_TYPE = "message_count";
    public static String SLEEP_THREAD_INTERRUPTED = "sleep_thread_interrupted";
    public static String ACCOUNT_RUNNING_STATUS = "account_running_status";
    public static String RUNNING = "running";
    public static String STOPED = "stopped";

    public static String LEDGER_UPDATED = "tech.overturn.LEDGER_UPDATED";

    public Ledger() {}

    public Ledger(Long parent_id, String entity, Date date, String type, String textval, Long longval, String description) {
        this.parent_id = parent_id;
        this.entity = entity;
        this.date = date;
        this.type = type;
        this.textval = textval;
        this.longval = longval;
        this.description = description;
    }

    public void log(SQLiteDatabase db, Context context) {
        Log.d("fcrow", toString());
        Orm.insert(db, Ledger.tableName, this);
        Intent intent = new Intent(LEDGER_UPDATED);
        context.sendBroadcast(intent);
    }

    public String toString() {
        return String.format("<Ledger %s -> %s/%d>", type, textval, longval);
    }

    private static String stackToString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
