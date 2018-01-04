package tech.overturn.crowmail;

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

    public Fetcher(Account a) {
        try {
            this.a = a;
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
            folder = (IMAPFolder) store.getFolder(url);
            folder.open(Folder.READ_ONLY);
            Log.d("fcrow", String.format("------- is folder open:%b", folder.isOpen()));
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
        }
    }

    public void loop() {
        Log.d("fcrow","------- in loop");
        /*
        Thread t = new Thread(new Runnable() {
            Integer one = 1000 * 60;
            @Override
            public void run() {
                try {
                    Thread.sleep(one);
                    folder.doCommand(new IMAPFolder.ProtocolCommand() {
                        public Object doCommand(IMAPProtocol p)
                                throws ProtocolException {
                            Status res = p.status("INBOX", new String[]{"uidnext"});
                            return null;
                        }
                    });
                } catch(Exception e) {
                    Log.d("fcrow","------- Error in Fetcher loop supervise "+ e.getMessage(), e);
                }
            }
        });
        t.start();
        */

        try {
            Log.d("fcrow", "--------- in try");
            Integer count = folder.getMessageCount();
            Log.d("fcrow", String.format("-------- message received count %d", count));
        } catch(Exception e) {
            Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
        }
    }
}
