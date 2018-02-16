package net.crowmail.model;

import net.crowmail.model.Data;

public class AccountData extends Data {
    public Integer _id;
    public String name;
    public String email;
    public String user;
    public String password;
    public String smtpHost;
    public Integer smtpPort;
    public String smtpSslType;
    public String imapHost;
    public Integer imapPort;
    public String imapSslType;
    public Integer uidnext;

    public Integer getId(){
        return this._id;
    }

    public void setId(Integer id){
        this._id = id;
    }
}
