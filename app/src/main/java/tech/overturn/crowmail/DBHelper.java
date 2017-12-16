package tech.overturn.crowmail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tech.overturn.crowmail.models.Account;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_FNAME = "Crowmail.db";

    DBHelper(Context ctx) {
        super(ctx, DB_FNAME, null, DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {}

    public void onUpgrade(SQLiteDatabase db, int old, int version) {
        if(version == 1){
            Log.d("fcrow","---------- version is 1 --------:"+Orm.getCreateTable(Account.class));
            //db.execSQL(Orm.getCreateTable(Account));
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
