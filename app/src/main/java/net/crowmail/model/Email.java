package net.crowmail.model;

import javax.mail.internet.InternetAddress;

import net.crowmail.model.Data;
import net.crowmail.util.DbField;

public class Email extends Data {
    public static String tableName = "email";
    @DbField
    String name;
    @DbField
    String email;

    public InternetAddress toIA(){
        try {
            return new InternetAddress(this.name, this.email);
        } catch(Exception e){};
        return null;
    }
}
