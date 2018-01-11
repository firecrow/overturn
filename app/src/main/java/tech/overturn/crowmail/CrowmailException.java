package tech.overturn.crowmail;

import tech.overturn.crowmail.models.Account;

public class CrowmailException extends RuntimeException {

    public String message;
    public String key;

    public Account a;
    public static String RETRY = "retry";
    public static String TIMEOUT = "timeout";
    public static String CONNECTION = "connection";
    public static String ERROR = "error";
    public static String UNKNOWN = "unknown";

    public CrowmailException(String key, String message, Exception cause, Account a){
        super(message, cause);
        this.key = key;
        this.a = a;
    }
}
