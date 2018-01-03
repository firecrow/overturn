package tech.overturn.crowmail.models;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.sql.SQLClientInfoException;
import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.ModelBase;
import tech.overturn.crowmail.Orm;

public class Account extends ModelBase {
    public AccountData data;
    public static String tableName = "account";

    public Account(){
        super();
        data = new AccountData();
    }

    public static Account byId(SQLiteDatabase db, Integer id){
        Account a = new Account();
        a.data = (AccountData) Orm.byId(db, Account.tableName, AccountData.class, id.intValue());
        return a;
    }
}
