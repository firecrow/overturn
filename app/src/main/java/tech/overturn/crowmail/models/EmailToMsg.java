package tech.overturn.crowmail.models;

import tech.overturn.crowmail.Data;

public class EmailToMsg extends Data {
    public static String tableName = "emailtomessage";
    Integer _id;
    String type;
    Integer email_id;
    Integer message_id;

    public Integer getId() {
        return this._id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
