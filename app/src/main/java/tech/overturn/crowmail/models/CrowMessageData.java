package tech.overturn.crowmail.models;

import android.view.Display;

import java.sql.Timestamp;

import tech.overturn.crowmail.Data;
import tech.overturn.crowmail.ModelBase;

public class CrowMessageData extends Data {
    public Integer _id;
    public String messageId;
    public Integer fromEmailId;
    public Integer returnPathEmailId;
    public Timestamp timestamp;
    public String subject;
    public String bodyText;
    public String bodyHtml;
    public Boolean isIncoming;

    public Integer getId() {
        return this._id;
    }

    public void setId(Integer id) {
        this._id = id;
    }
}
