package tech.overturn.crowmail;

import android.util.Log;
import android.widget.Toast;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

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
            a = a;
            url = new URLName(
                    "imaps",
                    a.data.imapHost,
                    a.data.imapPort,
                    "INBOX",
                    a.data.user,
                    a.data.password);
            props = System.getProperties();
            session = Session.getInstance(props);
            connect();
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher setup "+ e.getMessage(), e);
        }
    }

    public void connect() {
        try {
            Store store = session.getStore(url);
            store.connect();
            folder = (IMAPFolder) store.getFolder(url);
            folder.open(Folder.READ_ONLY);
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
        }
    }

    public void loop() {
        Thread t = new Thread(new Runnable() {
            Integer  ninemin = 1000 * 60 * 9;
            @Override
            public void run() {
                try {
                    Thread.sleep(ninemin);
                    folder.doCommand(new IMAPFolder.ProtocolCommand() {
                        public Object doCommand(IMAPProtocol p)
                                throws ProtocolException {
                            p.simpleCommand("NOOP", null);
                            return null;
                        }
                    });
                } catch(Exception e) {
                    Log.d("fcrow","------- Error in Fetcher loop supervise "+ e.getMessage(), e);
                }
            }
        });
        t.start();

        while(!Thread.interrupted()){
            Log.d("fcrow", "---- starting idle");
            try {
                while(true){
                    folder.idle();
                    manageFetch();
                }
            } catch(Exception e) {
                Log.d("fcrow","------- Error in Fetcher loop "+ e.getMessage(), e);
            }
        }

        if(t.isAlive()) {
            t.interrupt();
        }
    }

    public void manageFetch() {
        try {
            Integer count = folder.getMessageCount();
            Log.d("fcrow", String.format("message received count %d", count));
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher manageFetch "+ e.getMessage(), e);
        }
    }
}
