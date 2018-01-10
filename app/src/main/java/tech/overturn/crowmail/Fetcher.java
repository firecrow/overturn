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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.ErrorStatus;
import tech.overturn.crowmail.struct.QueueItem;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Fetcher extends QueueItem {

    Account a;
    IMAPFolder folder;
    Store store;
    URLName url;
    Session session;
    Long FETCH_DELAY = 1000 * 60 * 1L;
    Context context;
    String action = "fetch";

    DBHelper dbh;

    public Fetcher(Context context, Account a) {
        dbh = new DBHelper(context);
        this.a = a;
        this.context = context;
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        if (a.data.imapSslType.equals("STARTTLS")) {
            Log.d("frow","----STARTTLS");
            props.setProperty("mail.imap.auth", "true");
            props.setProperty("mail.imap.timeout", "5000");
            props.setProperty("mail.imap.connectiontimeout", "5000");
            props.setProperty("mail.imap.socketFactory.class", "tech.overturn.crowmail.SSLCrowFactory");
            props.setProperty("ssl.SocketFactory.provider", "tech.overturn.crowmail.SSLCrowFactory");
            props.setProperty("mail.imap.socketFactory.port", a.data.imapPort.toString());
            props.setProperty("mail.imap.starttls.enable", "true");
        } else {
            props.setProperty("mail.imaps.timeout", "5000");
            props.setProperty("mail.imaps.connectiontimeout", "5000");
        }
        return props;
    }

    public URLName getURLName(Properties props) {
        String protocol = a.data.imapSslType.equals("STARTTLS") : "imap" : "imaps"; 
        return new URLName(
                protocol,
                a.data.imapHost,
                a.data.imapPort,
                "Inbox",
                a.data.user,
                a.data.password);
    }

    private boolean connect() {
        Properties props = getProperties();
        URLName url = getURLName(props);
        Long start = new Date().getTime();
        try {
            session = Session.getInstance(props);
            start = new Date().getTime();
            store = session.getStore(url);
            start = new Date().getTime();
            store.connect();
            start = new Date().getTime();
            folder = (IMAPFolder) store.getFolder(url);
            start = new Date().getTime();
            folder.open(Folder.READ_ONLY);
            start = new Date().getTime();
            if(!folder.isOpen()){
                throw new Exception(String.format("Folder '%s' is not open in Fetcher.connect", folder.getName()));
            }
            return true;
        } catch(Exception e) {
            Log.d("fcrow","------- Error in Fetcher connect "+ e.getMessage(), e);
            String key = String.format("connect error millis: %d", (new Date().getTime()-start));
            ErrorStatus err = new ErrorStatus();
            err.key = key;
            err.message = e.getMessage();
            if(e.getCause() != null) {
                err.cause = e.getCause().getClass().getSimpleName();
            }else {
                err.cause = e.getClass().getSimpleName();
            }
            err.account_id = a.data._id;
            err.stack = ErrorStatus.stackToString(e);
            err.log(dbh.getWritableDatabase());
            err.sendNotify(context, true);
            return false;
        }
    }

    private Long getUidNext(IMAPFolder folder, final String folderName) throws MessagingException {
        return (Long) folder.doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p)
            throws ProtocolException {
               Status status = p.status(folderName, new String[]{"uidnext"});
                return status.uidnext;
            }
        });
    }

    private void notifyUpdates(Message[] msgs, Integer previous, Integer uidnext) throws MessagingException {
        String msg_group_key;
        if (a.data._id != null) {
            msg_group_key = String.format("%s%d", Global.CROWMAIL, a.data._id);
        } else {
            msg_group_key = Global.CROWMAIL;
        }

        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
        Notification sum = new Notification.Builder(context)
                .setSmallIcon(R.drawable.notif)
                .setContentTitle(a.data.email)
                .setGroupSummary(true)
                .setGroup(msg_group_key)
                .build();
        nmng.notify(msg_group_key, 0, sum);
        for (int i = 0; i < msgs.length; i++) {
            Log.d("fcrow", String.format("-------- new mail %s:%s", msgs[i].getFrom()[0], msgs[i].getSubject()));
            Notification n = new Notification.Builder(context)
                    .setContentTitle(msgs[i].getFrom()[0].toString())
                    .setContentText(msgs[i].getSubject())
                    .setSmallIcon(R.drawable.notif)
                    .setGroupSummary(false)
                    .setGroup(msg_group_key)
                    .build();
            nmng.notify(msg_group_key, previous+i, n);
        }
    }

    public Runnable getTask() {
        final ErrorStatus err = new ErrorStatus();
        err.key = "fire fetch";
        err.account_id = a.data._id;
        err.log(dbh.getWritableDatabase());
        err.sendNotify(context, false);

        final Account acc = this.a;
        return new Runnable() {
            @Override
            public void run() {
                Log.d("fcrow", "--- Fetch ran ---");
                return;
                /*
                try {
                    if(connect()) {
                        Long uidnext = getUidNext(folder, "Inbox");
                        Integer previous = (a.data.uidnext != null) ? a.data.uidnext : 1;
                        if (previous != uidnext.intValue()) {
                            Message[] msgs = folder.getMessagesByUID(a.data.uidnext, uidnext - 1);
                            notifyUpdates(msgs, previous, uidnext);
                            acc.data.uidnext = uidnext.intValue();
                            acc.save(dbh.getWritableDatabase());
                        }
                    }
                    if(folder != null && folder.isOpen()) {
                        folder.close(false);
                    }
                    if(store != null && store.isConnected()) {
                        store.close();
                    }
                } catch (Exception e) {
                    Log.d("fcrow", "------- Error in Fetcher loop " + e.getMessage(), e);
                    err.key = "fetch_generic";
                    err.message = e.getMessage();
                    err.cause = e.getClass().getSimpleName();
                    err.account_id = a.data._id;
                    err.log(dbh.getWritableDatabase());
                    err.sendNotify(context, false);
                }
                */
            }
        };
    }

    public Long getDelay() {
        return FETCH_DELAY;
    }

    public String getAction() {
        return "fetch";
    }

    public Long askRetry() {
        return -1L;
    }
}
