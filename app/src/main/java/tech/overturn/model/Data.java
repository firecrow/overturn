package tech.overturn.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CoordinatorLayout;

import tech.overturn.util.DbField;
import tech.overturn.util.Orm;

public class Data {

    @DbField
    public Long _id;

    @DbField
    public String _entity;

    @DbField
    public Long _parent_id;

    @DbField
    public String _parent_entity;

    public void save(Context context) {
        Orm.upsert(context, this);
    }
}
