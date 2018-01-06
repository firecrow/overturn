package tech.overturn.crowmail;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Status;


import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import tech.overturn.crowmail.models.Account;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Fetcher {

    Account a;
    IMAPFolder folder;
    URLName url;
    Properties props;
    Session session;
    int FETCH_DELAY = 1000 * 60 * 1;
    Context context;

    DBHelper dbh;

    public Fetcher(Context context, Account a) {
        dbh = new DBHelper(context);
        this.a = a;
        this.context = context;
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
            props = System.getProperties();
            session = Session.getInstance(props);
            Store store = session.getStore(url);
            store.connect();
            folder = (IMAPFolder) store.getFolder(url);
            folder.open(Folder.READ_ONLY);
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
        }
    }

    public void loop() {
        try {
            boolean first = true;
            while(true) {
                Long uidnext = (Long)folder.doCommand(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p)
                            throws ProtocolException {
                        Status status = p.status("Inbox", new String[]{"uidnext"});
                        return status.uidnext;
                    }
                });
                Log.d("fcrow", String.format("-------- next %d", uidnext));
                if (a.data.uidnext != uidnext.intValue()) {
                    NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
                    Message[] msgs = folder.getMessagesByUID(a.data.uidnext, uidnext-1);
                    if (first) {
                        Notification n = new Notification.Builder(context)
                                .setSmallIcon(R.drawable.notif)
                                .setGroupSummary(first)
                                .setGroup("CROWMAIL")
                                .build();
                        nmng.notify("CROWMAIL", 0, n);
                        first = false;
                    }
                    for(int i = 0; i < msgs.length; i++) {
                        Log.d("fcrow", String.format("-------- new mail %s:%s", msgs[i].getFrom()[0], msgs[i].getSubject()));
                        Notification n = new Notification.Builder(context)
                                .setContentTitle(msgs[i].getFrom()[0].toString())
                                .setContentText(msgs[i].getSubject())
                                .setSmallIcon(R.drawable.notif)
                                .setGroupSummary(first)
                                .setGroup("CROWMAIL")
                                .build();
                        nmng.notify("CROWMAIL", uidnext.intValue()+i, n);
                    }
                    a.data.uidnext = uidnext.intValue();
                    a.save(dbh.getWritableDatabase());
                }
                Thread.sleep(15 * 1000);
            }
        } catch (Exception e) {
            Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
        }
    }
}
