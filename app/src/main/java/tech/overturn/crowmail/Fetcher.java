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


import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.ErrorStatus;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Fetcher {

    Account a;
    IMAPFolder folder;
    Store store;
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

        props = System.getProperties();
        url = new URLName(
                "imaps",
                a.data.imapHost,
                a.data.imapPort,
                "Inbox",
                a.data.user,
                a.data.password);
        props = System.getProperties();
        props.setProperty("mail.imap.timeout", "3000");
    }

    public boolean connect() {
        Long start = new Date().getTime();
        try {
            session = Session.getInstance(props);
            store = session.getStore(url);
            store.connect();
            folder = (IMAPFolder) store.getFolder(url);
            folder.open(Folder.READ_ONLY);
            if(!folder.isOpen()){
                throw new Exception(String.format("Folder '%s' is not open in Fetcher.connect", folder.getName()));
            }
            return true;
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
            String key = String.format("connect error %d", (new Date().getTime()-start)/1000);
            new ErrorManager(context, dbh).error(e, key, a.data._id, 0);
            return false;
        }
    }

    public void loop() {
        new ErrorManager(context, dbh).error(null, "loop start", a.data._id, 0);
        try {
            while(true) {
                if(connect()) {
                    Long uidnext = (Long) folder.doCommand(new IMAPFolder.ProtocolCommand() {
                        public Object doCommand(IMAPProtocol p)
                                throws ProtocolException {
                            Status status = p.status("Inbox", new String[]{"uidnext"});
                            return status.uidnext;
                        }
                    });
                    Log.d("fcrow", String.format("-------- next %d", uidnext));
                    Integer previous = a.data.uidnext;
                    if (previous != uidnext.intValue()) {
                        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
                        Message[] msgs = folder.getMessagesByUID(a.data.uidnext, uidnext - 1);
                        Notification sum = new Notification.Builder(context)
                                .setSmallIcon(R.drawable.notif)
                                .setGroupSummary(true)
                                .setGroup("CROWMAIL")
                                .build();
                        nmng.notify("CROWMAIL", 0, sum);
                        for (int i = 0; i < msgs.length; i++) {
                            Log.d("fcrow", String.format("-------- new mail %s:%s", msgs[i].getFrom()[0], msgs[i].getSubject()));
                            Notification n = new Notification.Builder(context)
                                    .setContentTitle(msgs[i].getFrom()[0].toString())
                                    .setContentText(msgs[i].getSubject())
                                    .setSmallIcon(R.drawable.notif)
                                    .setGroupSummary(false)
                                    .setGroup("CROWMAIL")
                                    .build();
                            nmng.notify("CROWMAIL", previous + i, n);
                        }
                        a.data.uidnext = uidnext.intValue();
                        a.save(dbh.getWritableDatabase());
                    }
                }
                folder.close(false);
                store.close();
                Thread.sleep(FETCH_DELAY);
            }

        } catch (InterruptedException e) {
            Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
            new ErrorManager(context, dbh).error(e, "fetch_interupt", a.data._id, 0);
        } catch (MessagingException e) {
            Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
            new ErrorManager(context, dbh).error(e, "fetch", a.data._id, 0);
        } catch (Exception e) {
            Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
            new ErrorManager(context, dbh).error(e, "fetch_generic", a.data._id, 0);
        }
        new ErrorManager(context, dbh).error(null, "loop finish", a.data._id, 0);
    }
}
