package tech.overturn.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import tech.overturn.util.DbField;

public class Email extends Data {

    public static String parent_entity = Account.tableName;
    public static String tableName = "email";

    @DbField
    public String from;

    @DbField
    public Date date;

    @DbField
    public String subject;

    @DbField
    public String raw;

    public Email(Long account_id) {
        this._parent_id = account_id;
        this._entity = Email.tableName;
    }

    public void setRawMessage(Message msg) {
        Log.d("fcrow","------------------ setRawMessage");
        BufferedReader bs = null;
        try {
            Log.d("fcrow","------------------ about to init");
            bs = new BufferedReader(new InputStreamReader(msg.getInputStream()));
            Log.d("fcrow","------------------ initialized the bullshit");
            String buff;
            this.raw = "";
            while ((buff = bs.readLine()) != null) {
                Log.d("fcrow","------------------ chunk:"+buff);
                this.raw += buff;
            }
        } catch(IOException e) {
            Log.e("fcrow", "----------- oops io exception in setRawMessage");
        } catch(MessagingException e) {
            Log.e("fcrow", "----------- oops messaging exception in setRawMessage");
        } finally {
            try {
                bs.close();
            } catch(IOException e) {}
        }
    }
}
