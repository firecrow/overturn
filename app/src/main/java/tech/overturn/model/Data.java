package tech.overturn.model;

import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CoordinatorLayout;

import tech.overturn.util.DbField;
import tech.overturn.util.Orm;

public class Data {
    @DbField
    public Long _id;
    @DbField
    public String _entity;

    public void save(SQLiteDatabase db) {
        if (_id != null) {
            Orm.update(db, Account.tableName, this);
        } else {
            Orm.insert(db, Account.tableName, this);
        }
    }
}
