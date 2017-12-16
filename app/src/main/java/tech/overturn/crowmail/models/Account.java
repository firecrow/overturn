package tech.overturn.crowmail.models;

import tech.overturn.crowmail.ModelIfc;

public class Account extends ModelIfc {
    String name;
    String user;
    String password;
    String smtpHost;
    Integer smtpPort;
    String smtpSslType;
    String imapHost;
    Integer imapPort;
    String imapSslType;
}
