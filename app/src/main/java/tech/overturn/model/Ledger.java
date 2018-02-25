package tech.overturn.model;

import tech.overturn.util.DbField;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ledger {
    public static String SCHEMA = "ledger";

    public Long _id;
    public Long parent_id;
    public String entity;
    public Date date;
    public String type;
    public Long longval;
    public String strval;
    public List<Ledger> children;

    public static String ERROR_TYPE = "error";
    public static String NETWORK_STATUS_TYPE = "network_status";
    public static String NETWORK_UNREACHABLE = "network_unreachable";
    public static String MESSAGING_ERROR = "messaging_error";
    public static String INFO_TYPE = "info";
    public static String UID_NEXT = "uidnext";
    public static String MESSAGE_COUNT_TYPE = "message_count";
    public static String FETCH_TASK_CREATED = "fetch_task_created";
    public static String FETCH_RANGE = "fetch_range";
    public static String LATEST_FETCH = "latest_fetch";
    public static String SLEEP_THREAD_INTERRUPTED = "sleep_thread_interrupted";
    public static String ACCOUNT_RUNNING_STATUS = "account_running_status";
    public static String RUNNING = "running";
    public static String QUEUED = "queued";
    public static String STOPED = "stopped";
    public static String STOPING = "stoping";
    public static String ERROR = "error";

    public static String LEDGER_UPDATED = "tech.overturn.LEDGER_UPDATED";

    public Ledger() {
        children = new ArrayList<Ledger>();
    }

    public Ledger(String entity) {
        children = new ArrayList<Ledger>();
        this.entity = entity;
    }

    public Ledger(Long parent_id, String entity, Date date, String type, Long longval, String strval) {
        children = new ArrayList<Ledger>();
        this.parent_id = parent_id;
        this.entity = entity;
        this.date = date;
        this.type = type;
        this.strval = strval;
        this.longval = longval;
    }

    public String toString() {
        return String.format("<Ledger %s -> %s/%d>", type, strval, longval);
    }
}
