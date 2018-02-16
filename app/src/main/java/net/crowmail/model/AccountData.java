package net.crowmail.model;

import net.crowmail.model.Data;

public class AccountData extends Data {
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
}
