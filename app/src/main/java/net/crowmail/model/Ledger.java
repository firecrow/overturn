package net.crowmail.model;

import net.crowmail.R;
import net.crowmail.util.CrowNotification;
import net.crowmail.util.Global;
import net.crowmail.util.Orm;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Ledger extends Data {
    public static String tableName  = "ledger";
    public Long account_id;
    public Date date;
    public String type;
    public String description;
    public Long longval;
    public String textval;

    public static String ERROR_TYPE = "error";
    public static String NETWORK_STATUS_TYPE = "network_status";
    public static String NETWORK_UNREACHABLE = "network_unreachable";
    public static String INFO_TYPE = "info";
    public static String LATEST_FETCH_TYPE = "latest_fetch";
    public static String MESSAGE_COUNT_TYPE = "message_count";
    public static String SLEEP_THREAD_INTERRUPTED = "sleep_thread_interrupted";

    public static String LEDGER_UPDATED = "net.crowmail.LEDGER_UPDATED";

    public Ledger() {}

    public Ledger(Long account_id, Date date, String type, String textval, Long longval, String description) {
        this.account_id = account_id;
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
