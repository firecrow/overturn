package tech.overturn.crowmail.models;

import java.util.Date;

import tech.overturn.crowmail.Data;


public class ErrorStatus extends Data {
    public static String tableName  = "errorstatus";
    public Integer date;
    public Integer _id;
    public String key;
    public String message;
    public String cause;
    public Integer message_id;
    public Integer account_id;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
