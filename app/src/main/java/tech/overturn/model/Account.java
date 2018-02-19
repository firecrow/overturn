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
        Account a = (Account) Orm.byId(db, Account.tableName, Account.class, id);
        return a;
    }

    public static List<Long> allRunningIds(Context context) {
        List<Long> running = new ArrayList<Long>();
        for (Long id: allIds(context)) {
            if(isRunningById(context, id)){
                running.add(id);
            }
        }
        return running;
    }

    public static List<Long> allIds(Context context) {
        List<Long> ids =  new ArrayList<Long>();
        List<Id> _ids = (List<Id>) Orm.byQuery(Global.getWriteDb(context),
                Account.tableName,
                Id.class,
                null,
                null,
                null,
                null);
        for(Id id: _ids) {
            ids.add(id._id);
        }
        return ids;
    }

    public static Boolean isRunningById(Context context, Long id) {
        List<Ledger> ldata = (List<Ledger>) Orm.byQuery(Global.getWriteDb(context),
                Ledger.tableName,
                Ledger.class,
                String.format("type = ? AND entity = ? AND parent_id = ?"),
                new String[]{Ledger.ACCOUNT_RUNNING_STATUS, Account.tableName, id.toString()},
                "date desc",
                1);
        return ldata.size() > 0 && ldata.get(0).textval.equals(Ledger.RUNNING);
    }
}
