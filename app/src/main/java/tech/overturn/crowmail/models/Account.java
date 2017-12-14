package tech.overturn.crowmail.models;

public class Account {
    static String tableName = "account";
    static String abbrv = "a";
    Integer _id;
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
