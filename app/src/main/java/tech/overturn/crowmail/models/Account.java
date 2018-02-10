package tech.overturn.crowmail.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.sql.SQLClientInfoException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.overturn.crowmail.Data;
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

    public void save(SQLiteDatabase db) {
        if (data._id != null) {
            Orm.update(db, Account.tableName, data);
        } else {
            Orm.insert(db, Account.tableName, data);
        }
    }

    public void setFetchLedger(SQLiteDatabase db, Context context, Long uidnext) {
        Ledger ledger = new Ledger();
        ledger.type = Ledger.LATEST_FETCH_TYPE;
        ledger.date = new Date();
        ledger.account_id = this.data._id;
        ledger.longval = uidnext;
        ledger.log(db, context);
    }

    public Ledger getFetchLedger(SQLiteDatabase db){
        String qry = String.format("select type, date, account_id, longval from ledger " +
                    "where account_id = ? and type = 'fetch' "+
                    "order by date "+
                    "limit 1",
                this.data._id);
        List<? extends Data> results = Orm.byQueryRaw(db,
                AccountData.class,
                new String[]{"type", "date", "account_id", "longval"},
                qry,
                new String[]{this.data._id.toString()});
        if (results.size() > 0) {
            return (Ledger) results.get(0);
        } else {
            return null;
        }
    }
}
