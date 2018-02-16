package net.crowmail.model;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sun.mail.iap.ByteArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import net.crowmail.util.Orm;
import net.crowmail.vector.EmailWithType;

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

    SQLiteDatabase db;

    public CrowMessage(SQLiteDatabase db) {
        this.db = db;
        this.data = new CrowMessageData();
    }

    public static CrowMessage byId(SQLiteDatabase db, Integer id) {
        CrowMessage msg = new CrowMessage(db);
        msg.data = (CrowMessageData) Orm.byId(db, CrowMessage.tableName, CrowMessageData.class, id);
        msg.loadAddresses();
        return msg;
    }

    public void loadAddresses(){
        try {
            String qry = "select e.name, e.email, m2m.type "
                    + "from email e join emailtomessage m2m on e._id = m2m.email_id "
                    + "join message m on m._id = m2m.message_id where m._id = ?";
            String[] cols = {"name", "email", "type"};
            String[] args = {data._id.toString()};
            List<EmailWithType> emails = (List<EmailWithType>) Orm.byQueryRaw(db, EmailWithType.class, cols, qry, args);
            List<InternetAddress> to = new ArrayList<InternetAddress>();
            List<InternetAddress> cc = new ArrayList<InternetAddress>();
            List<InternetAddress> bcc = new ArrayList<InternetAddress>();
            for (EmailWithType e : emails) {
                if (e.type.equals("TO")) {
                    to.add(new InternetAddress(e.email, e.name));
                } else if (e.type.equals("CC")) {
                    cc.add(new InternetAddress(e.email, e.name));
                } else if (e.type.equals("BCC")) {
                    bcc.add(new InternetAddress(e.email, e.name));
                } else if (e.type.equals("FROM")) {
                    this.from = new InternetAddress(e.email, e.name);
                }
            }
            this.to = to.toArray(new InternetAddress[to.size()]);
            this.cc = cc.toArray(new InternetAddress[cc.size()]);
            this.bcc = bcc.toArray(new InternetAddress[bcc.size()]);
        } catch(Exception e){
            Log.e("fcrow", "-------------------- error in loadAddresses:" + e.getMessage(), e);
        }
    }

    public void save(){
        if(data._id != null) {
            Orm.update(db, tableName, data);
        } else {
            Orm.insert(db, tableName, data);
        }
        saveAddress("FROM", from);
        //saveAddress("FROM", returnPath);
        if(to != null) {
            saveAddresses("TO", to);
        }
        if(cc != null) {
            saveAddresses("CC", cc);
        }
        if(bcc != null) {
            saveAddresses("BCC", bcc);
        }
    }

    public void saveAddresses(String type, InternetAddress[] addrs) {
        for(int i = 0; i < addrs.length; i++){
            saveAddress(type, addrs[i]);
        }
    }

    public void saveAddress(String type, InternetAddress addr) {
        Long email_id = genEmailId(addr);
        EmailToMsg m2m = new EmailToMsg();
        m2m.email_id = email_id;
        m2m.message_id = this.data._id;
        m2m.type = type;
        Orm.insert(db, EmailToMsg.tableName, m2m);
    }

    public Long genEmailId(InternetAddress address) {
        String sql = "select _id from email where email = ?";
        String args[] = new String[]{address.getAddress()};
        List<? extends Data> results = Orm.byQueryRaw(db, Email.class, new String[]{"_id"}, sql, args);
        Long email_id;
        if(results.size() > 0) {
            email_id = ((Email)results.get(0))._id;
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
