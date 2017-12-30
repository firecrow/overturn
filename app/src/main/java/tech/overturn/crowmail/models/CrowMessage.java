package tech.overturn.crowmail.models;

import android.database.sqlite.SQLiteDatabase;

import com.sun.mail.iap.ByteArray;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;

import tech.overturn.crowmail.Data;
import tech.overturn.crowmail.ModelBase;
import tech.overturn.crowmail.Orm;

public class CrowMessage extends ModelBase {
    public static String tableName = "message";
    public CrowMessageData data;
    public InternetAddress[] to;
    public InternetAddress[] cc;
    public InternetAddress[] bcc;
    public InternetAddress returnPath;
    public Map<String, Date> relayDates;
    public List<ByteArray> attachments;
    public InternetAddress from;

    public CrowMessage() {
        this.data = new CrowMessageData();
        //this.loadAddresses();
    }

    public void save(SQLiteDatabase db){
        /*
        saveAddresses(Message.RecipientType.TO,to);
        saveAddresses(Message.RecipientType.CC, cc);
        saveAddresses(Message.RecipientType.BCC, bcc);
        data.returnPathEmailId = genEmailId(this.returnPath);
        */
        data.fromEmailId = genEmailId(db, this.from);
        if(data._id != null) {
            Orm.update(db, tableName, data);
        } else {
            Orm.insert(db, tableName, data);
        }
    }

    public void saveAddresses(SQLiteDatabase db, Message.RecipientType type, InternetAddress[] addrs) {
        for(int i = 0; i < addrs.length; i++){
            Integer email_id = genEmailId(db, addrs[i]);
            EmailToMsg m2m = new EmailToMsg();
            m2m.email_id = email_id;
            m2m.message_id = this.data._id;
            m2m.type = type.toString();
            Orm.insert(db, "emailtomessage", m2m);
        }
    }

    public Integer genEmailId(SQLiteDatabase db, InternetAddress address) {
        String sql = "select * from email where email = ?";
        String args[] = new String[]{address.getAddress()};
        List<? extends Data> results = Orm.byQueryRaw(db, CrowMessageData.class, sql, args);
        Integer email_id;
        if(results.size() > 0) {
            email_id = ((CrowMessageData)results.get(0))._id;
        } else {
            Email email = new Email();
            email.name = address.getPersonal();
            email.email = address.getAddress();
            Orm.insert(db, Email.tableName, email);
            email_id = email._id;
        }
        return email_id;
    }
}
