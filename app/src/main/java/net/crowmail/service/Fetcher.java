package net.crowmail.service;

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
import android.view.InputQueue;
import android.widget.Toast;

import com.sun.mail.iap.ConnectionException;
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

import net.crowmail.R;
import net.crowmail.model.Account;
import net.crowmail.model.Ledger;
import net.crowmail.util.CrowNotification;
import net.crowmail.util.Global;

public class Fetcher {

    Account a;
    Context context;

    Properties props;
    URLName url;
    Session session;
    Store store;
    IMAPFolder folder;

    public static Long TIMEOUT = 1000 * 5L;
    public static Long FETCH_DELAY = 1000 * 15L;


    public Fetcher(Context context, Account a) {
        this.a = a;
        this.context = context;
        this.props = getProperties();
        url = getURLName(this.props);
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        if (a.imapSslType.equals("STARTTLS")) {
            props.setProperty("mail.imap.auth", "true");
            props.setProperty("mail.imap.timeout", TIMEOUT.toString());
            props.setProperty("mail.imap.connectiontimeout", TIMEOUT.toString());
            props.setProperty("mail.imap.socketFactory.class", "net.crowmail.SSLCrowFactory");
            props.setProperty("ssl.SocketFactory.provider", "net.crowmail.SSLCrowFactory");
            props.setProperty("mail.imap.socketFactory.port", a.imapPort.toString());
            props.setProperty("mail.imap.starttls.enable", "true");
        } else {
            props.setProperty("mail.imaps.timeout", TIMEOUT.toString());
            props.setProperty("mail.imaps.connectiontimeout", TIMEOUT.toString());
        }
        return props;
    }

    public URLName getURLName(Properties props) {
        String protocol = a.imapSslType.equals("STARTTLS") ? "imap" : "imaps";
        return new URLName(
                protocol,
                a.imapHost,
                a.imapPort,
                "Inbox",
                a.user,
                a.password);
    }

    private void connect() throws MessagingException {
        session = Session.getInstance(props);
        store = session.getStore(url);
        store.connect();
        folder = (IMAPFolder) store.getFolder(url);
        folder.open(Folder.READ_ONLY);
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

    private Long fetchMail() throws MessagingException {
        Long uidnext = getUidNext(folder, "Inbox");
        Integer previous = (a.uidnext != null && a.uidnext != 0) ? a.uidnext : 1;
        if (previous != uidnext.intValue()) {
            Message[] msgs = folder.getMessagesByUID(previous, uidnext - 1);
            notifyUpdates(msgs);
            a.uidnext = uidnext.intValue();
            a.save(Global.getWriteDb(context));
            Log.d("fcrow", String.format("---- fetching %d..%d", previous, uidnext));
            new Ledger(
                    a._id,
                    new Date(),
                    Ledger.MESSAGE_COUNT_TYPE,
                    String.format("messages %d..%d", previous, uidnext),
                    Long.valueOf(msgs.length),
                    null
            ).log(Global.getWriteDb(context), context);
        }
        if(folder != null && folder.isOpen()) {
            folder.close(false);
        }
        if(store != null && store.isConnected()) {
            store.close();
        }
        return uidnext;
    }

    private void notifyUpdates(Message[] msgs) throws MessagingException {
        Log.d("fcrow", String.format("---- %d emails", msgs.length));
        String msg_group_key;
        if (a._id != null) {
            msg_group_key = String.format("%s%d", Global.CROWMAIL, a._id);
        } else {
            msg_group_key = Global.CROWMAIL;
        }
        for (int i = 0; i < msgs.length; i++) {
            String from = msgs[i].getFrom()[0].toString();
            String subject = msgs[i].getSubject();
            new CrowNotification(context).send(from, subject, msg_group_key, R.drawable.notif, false);
        }
    }

    public void loop(){
        new Ledger(
                a._id,
                new Date(),
                Ledger.INFO_TYPE,
                "fetch task created",
                null,
                null
        ).log(Global.getWriteDb(context), context);
        final Fetcher self = this;
        final Runnable runnable = new Runnable() {
            final Account _account = a;
            @Override
            public void run() {
                while (true) {
                    Long delay = Fetcher.FETCH_DELAY;
                    Date startDebug = new Date();
                    Long uidnext;
                    try {
                        connect();
                        uidnext = fetchMail();
                        new Ledger(
                                _account._id,
                                new Date(),
                                Ledger.LATEST_FETCH_TYPE,
                                String.format("duration %d",new Date().getTime() - startDebug.getTime()),
                                uidnext,
                                null
                        ).log(Global.getWriteDb(context), context);
                    } catch (Exception e) {
                        delay *= 3;
                        new Ledger(
                                _account._id,
                                new Date(),
                                Ledger.NETWORK_UNREACHABLE,
                                _account.imapHost,
                                new Date().getTime() - startDebug.getTime(),
                                Global.stackToString(e)
                        ).log(Global.getWriteDb(context), context);
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        new Ledger(
                                _account._id,
                                new Date(),
                                Ledger.SLEEP_THREAD_INTERRUPTED,
                                null,
                                new Date().getTime() - startDebug.getTime(),
                                Global.stackToString(e)
                        ).log(Global.getWriteDb(context), context);
                        break;
                    }
                }
            }
        };
        new Thread(runnable).start();
    }
}
