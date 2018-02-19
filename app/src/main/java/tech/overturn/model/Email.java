package tech.overturn.model;

import javax.mail.internet.InternetAddress;

import tech.overturn.model.Data;
import tech.overturn.util.DbField;

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
