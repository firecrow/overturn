package tech.overturn.crowmail.util;

import tech.overturn.crowmail.model.Account;

public class CrowmailException extends RuntimeException {

    public String message;
    public CmeKey key;

    public Account a;
    public static CmeKey RETRY = new CmeKey("retry");
    public static CmeKey TIMEOUT = new CmeKey("timeout");
    public static CmeKey CONNECTION = new CmeKey("connection");
    public static CmeKey ERROR = new CmeKey("error");
    public static CmeKey UNKNOWN = new CmeKey("unknown");

    public CrowmailException(CmeKey key, String message, Exception cause, Account a){
        super(message, cause);
        this.key = key;
        this.a = a;
    }

    public static class CmeKey {
        String string;
        public CmeKey(String string) { this.string = string;}
        public String toString() { return this.string; }
    }
}
