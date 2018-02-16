package net.crowmail.model;

import android.view.Display;

import java.sql.Timestamp;

import net.crowmail.model.Data;
import net.crowmail.model.ModelBase;

public class CrowMessageData extends Data {
    public String messageId;
    public Timestamp timestamp;
    public String subject;
    public String bodyText;
    public String bodyHtml;
    public Boolean isIncoming;
}
