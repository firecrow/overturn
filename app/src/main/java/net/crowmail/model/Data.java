package net.crowmail.model;

import android.database.sqlite.SQLiteDatabase;

import net.crowmail.util.DbField;
import net.crowmail.util.Orm;

public class Data {
    @DbField
    public Long _id;

    public void save(SQLiteDatabase db) {
        if (_id != null) {
            Orm.update(db, Account.tableName, this);
        } else {
            Orm.insert(db, Account.tableName, this);
        }
    }
}
