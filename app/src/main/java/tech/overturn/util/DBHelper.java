package tech.overturn.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tech.overturn.util.Orm;
import tech.overturn.model.Account;
import tech.overturn.model.CrowMessage;
import tech.overturn.model.Email;
import tech.overturn.model.EmailToMsg;
import tech.overturn.model.Ledger;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_FNAME = "crowmail.db";

    DBHelper(Context ctx) {
        super(ctx, DB_FNAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Orm.getCreateTable(Account.tableName, Account.class));
        db.execSQL(Orm.getCreateTable(CrowMessage.tableName, CrowMessage.class));
        db.execSQL(Orm.getCreateTable(Email.tableName, Email.class));
        db.execSQL(Orm.getCreateTable(EmailToMsg.tableName, EmailToMsg.class));
        db.execSQL(Orm.getCreateTable(Ledger.tableName, Ledger.class));
    }

    public void onUpgrade(SQLiteDatabase db, int old, int version) {}

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
