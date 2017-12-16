package tech.overturn.crowmail.models;

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
}
