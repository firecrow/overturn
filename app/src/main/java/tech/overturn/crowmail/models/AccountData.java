package tech.overturn.crowmail.models;

import tech.overturn.crowmail.Data;

public class AccountData extends Data {
    public String name;
    public String user;
    public String password;
    public String smtpHost;
    public Integer smtpPort;
    public String smtpSslType;
    public String imapHost;
    public Integer imapPort;
    public String imapSslType;
}
