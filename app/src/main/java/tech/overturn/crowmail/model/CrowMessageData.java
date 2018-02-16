package tech.overturn.crowmail.model;

import android.view.Display;

import java.sql.Timestamp;

import tech.overturn.crowmail.model.Data;
import tech.overturn.crowmail.model.ModelBase;

public class CrowMessageData extends Data {
    public Integer _id;
    public String messageId;
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
