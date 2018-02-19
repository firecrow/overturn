package tech.overturn.model;

import tech.overturn.model.Data;
import tech.overturn.util.DbField;

public class EmailToMsg extends Data {
    public static String tableName = "emailtomessage";
    @DbField
    String type;
    @DbField
    Long email_id;
    @DbField
    Long message_id;
}
