package tech.overturn.crowmail;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Status;


import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import tech.overturn.crowmail.models.Account;

public class Fetcher {

    Account a;
    IMAPFolder folder;
    URLName url;
    Properties props;
    Session session;
    int FETCH_DELAY = 1000 * 60 * 1;

    DBHelper dbh;

    public Fetcher(Context context, Account a) {
        dbh = new DBHelper(context);
        this.a = a;
        try {
            connect();
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher setup "+ e.getMessage(), e);
        }
    }

    public void connect() {
        try {
            Properties props = System.getProperties();
            url = new URLName(
                    "imaps",
                    a.data.imapHost,
                    a.data.imapPort,
                    "Inbox",
                    a.data.user,
                    a.data.password);
            Log.d("fcrow",String.format("----------- url:%s", url));
            props = System.getProperties();
            session = Session.getInstance(props);
            Store store = session.getStore(url);
            store.connect();
            Log.d("fcrow", String.format("------- is connected:%b", store.isConnected()));
            folder = (IMAPFolder) store.getFolder(url);
            folder.open(Folder.READ_ONLY);
            Log.d("fcrow", String.format("------- is folder open:%b", folder.isOpen()));
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
        }
    }

    public void loop() {
        Log.d("fcrow", "--------- loop");
        while(true) {
            try {
                Long uidnext = (Long)folder.doCommand(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p)
                            throws ProtocolException {
                        Status status = p.status("Inbox", new String[]{"uidnext"});
                        return status.uidnext;
                    }
                });
                Log.d("fcrow", String.format("-------- next2 from cmd %d", uidnext));
                if (a.data.uidnext != uidnext.intValue()) {
                    Log.d("fcrow", String.format("-------- new mail"));
                    a.data.uidnext = uidnext.intValue();
                    a.save(dbh.getWritableDatabase());
                }
                Thread.sleep(15 * 1000);
            } catch (Exception e) {
                Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
            }
        }
    }
}
