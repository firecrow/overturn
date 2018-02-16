package tech.overturn.crowmail.model;

import javax.mail.internet.InternetAddress;

import tech.overturn.crowmail.model.Data;

public class Email extends Data {
    public static String tableName = "email";
    Integer _id;
    String name;
    String email;

    public InternetAddress toIA(){
        try {
            return new InternetAddress(this.name, this.email);
        } catch(Exception e){};
        return null;
    }

    public Integer getId() {
        return this._id;
    }

    public void setId(Integer id) {
        this._id = id;
    }

}
