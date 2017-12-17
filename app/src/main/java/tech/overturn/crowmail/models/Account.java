package tech.overturn.crowmail.models;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.ModelIfc;

public class Account extends ModelIfc {
    public String name;
    public String user;
    public String password;
    public String smtpHost;
    public Integer smtpPort;
    public String smtpSslType;
    public String imapHost;
    public Integer imapPort;
    public String imapSslType;

    public Map<String, Long> ui;

    public Account(){
        super();
        ui = new HashMap<String, Long>();
    }
}
