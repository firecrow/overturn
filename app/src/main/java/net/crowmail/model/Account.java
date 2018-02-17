package net.crowmail.model;

import android.database.sqlite.SQLiteDatabase;

import net.crowmail.util.DbField;
import net.crowmail.util.Orm;

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

}
