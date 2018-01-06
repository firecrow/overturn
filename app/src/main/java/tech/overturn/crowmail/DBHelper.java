package tech.overturn.crowmail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.AccountData;
import tech.overturn.crowmail.models.CrowMessage;
import tech.overturn.crowmail.models.CrowMessageData;
import tech.overturn.crowmail.models.Email;
import tech.overturn.crowmail.models.EmailToMsg;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 5;
    public static final String DB_FNAME = "Crowmail.db";

    DBHelper(Context ctx) {
        super(ctx, DB_FNAME, null, DB_VERSION);
        Log.d("fcrow", "---------------- db helper");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Orm.getCreateTable(Account.tableName, AccountData.class));
        db.execSQL(Orm.getCreateTable(CrowMessage.tableName, CrowMessageData.class));
        db.execSQL(Orm.getCreateTable(Email.tableName, Email.class));
        db.execSQL(Orm.getCreateTable(EmailToMsg.tableName, EmailToMsg.class));
    }

    public void onUpgrade(SQLiteDatabase db, int old, int version) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
