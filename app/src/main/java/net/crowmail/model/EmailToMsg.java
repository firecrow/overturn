package net.crowmail.model;

import net.crowmail.model.Data;
import net.crowmail.util.DbField;

public class EmailToMsg extends Data {
    public static String tableName = "emailtomessage";
    @DbField
    String type;
    @DbField
    Long email_id;
    @DbField
    Long message_id;
}
