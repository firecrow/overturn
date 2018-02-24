package tech.overturn.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
    public String smtpHost;

    @DbField
    public Integer smtpPort;

    @DbField
    public String smtpSslType;

    @DbField
    public String imapHost;

    @DbField
    public Integer imapPort;

    @DbField
    public String imapSslType;

    @DbField
    public Integer uidnext;

    public Account(){
        super();
    }

    public static Account byId(SQLiteDatabase db, Long id){
        Account a = (Account) Orm.byId(db, Account.class, Account.tableName, id);
        return a;
    }

    public static List<Long> allIds(Context context) {
        List<Id> ids =  Orm.idsByEntity(Global.getWriteDb(context), Account.tableName);
        List<Long> longs = new ArrayList<Long>();
        for(Id id: ids) {
            longs.add(id._id);
        }
        return longs;
    }

    public static String runStateForId(Context context, Long id) {
        Ledger state = Orm.getAttribute(Global.getReadDb(context),
                Ledger.ACCOUNT_RUNNING_STATUS, id, Account.tableName);
        if (state == null) {
            return Ledger.STOPED;
        } else {
            return state.strval;
        }
    }
}
