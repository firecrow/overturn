package net.crowmail.model;

import net.crowmail.model.Data;

public class EmailToMsg extends Data {
    public static String tableName = "emailtomessage";
    String type;
    Long email_id;
    Long message_id;
}
