package tech.overturn.crowmail.models;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.ModelBase;

public class Account extends ModelBase {
    public AccountData data;
    public static String tableName = "account";

    public Account(){
        super();
        data = new AccountData();
    }
}
