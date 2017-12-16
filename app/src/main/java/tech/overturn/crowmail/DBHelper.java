package tech.overturn.crowmail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import tech.overturn.crowmail.models.Account;

/**
 * Created by firecrow on 12/15/17.
 */

public class DBHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_FNAME 'Crowmail.db';

    Orm(Context ctx) {
        super(ctx, DB_FNAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Orm.getCreateTable(Account));
    }
}
