package tech.overturn.crowmail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import tech.overturn.crowmail.models.Account;

public class DBHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_FNAME 'Crowmail.db';

    DBHelper(Context ctx) {
        super(ctx, DB_FNAME, null, DB_VERSION);
    }

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase db, int old, int version) {
        if(version == 1){
            Log.d('----------- version is 1 --------:'+Orm.getCreateTable(Account));
            //db.execSQL(Orm.getCreateTable(Account));
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
