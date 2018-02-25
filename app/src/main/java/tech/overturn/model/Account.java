package tech.overturn.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import tech.overturn.util.DbField;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

import java.util.ArrayList;
import java.util.List;

public class Account extends ModelBase {
    public static String tableName = "account";

    @DbField
    public String name;

    @DbField
    public String email;

    @DbField
    public String user;

    @DbField
    public String password;

    @DbField
    public String imapHost;

    @DbField
    public Long imapPort;

    @DbField
    public String imapSslType;

    @DbField
    public Long uidnext;

    public Account(){
        super();
        this._entity = tableName;
    }

    public static Account byId(Context context, Long id){
        Account a = (Account) Orm.byId(context, Account.class, Account.tableName, id);
        return a;
    }

    public static List<Long> allIds(Context context) {
        List<Id> ids =  Orm.idsByEntity(context, Account.tableName);
        List<Long> longs = new ArrayList<Long>();
        for(Id id: ids) {
            longs.add(id._id);
        }
        return longs;
    }

    public static String runStateForId(Context context, Long id) {
        Ledger state = Orm.getAttribute(context,
                Ledger.ACCOUNT_RUNNING_STATUS, id, Account.tableName);
        Log.d("fcrow", String.format("--------------- state is? %b", state == null));
        if (state == null) {
            return Ledger.STOPED;
        } else {
            return state.strval;
        }
    }
}
