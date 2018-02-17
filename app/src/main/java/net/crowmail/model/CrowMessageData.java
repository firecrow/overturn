package net.crowmail.model;

import android.view.Display;

import java.sql.Timestamp;

import net.crowmail.model.Data;
import net.crowmail.model.ModelBase;
import net.crowmail.util.DbField;

public class CrowMessageData extends Data {
    @DbField
    public String messageId;
    @DbField
    public Timestamp timestamp;
    @DbField
    public String subject;
    @DbField
    public String bodyText;
    @DbField
    public String bodyHtml;
    @DbField
    public Boolean isIncoming;
}
