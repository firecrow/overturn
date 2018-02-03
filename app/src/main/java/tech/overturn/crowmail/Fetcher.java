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
import android.view.InputQueue;
import android.widget.Toast;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Status;


import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.CrowMessage;
import tech.overturn.crowmail.models.ErrorStatus;
import tech.overturn.crowmail.QueueItem;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Fetcher implements QueueItem {

    Account a;
    Context context;
    DBHelper dbh;

    Properties props;
    URLName url;
    Session session;
    Store store;
    IMAPFolder folder;

    String action = "fetch";
    public Integer failCount = 0;
    public Date latestFailure;
    public Long MAX_ADJUSTED_FAIL = 5L;
    public Long RELEASE_TIME = 1000 * 60 * 15L;
    public Long TIMEOUT = 1000 * 15L;
    //Long FETCH_DELAY = 1000 * 60 * 1L;
    Long FETCH_DELAY = 1000 * 15L;


    public Fetcher(Context context, Account a) {
        dbh = new DBHelper(context);
        this.a = a;
        this.context = context;
        this.props = getProperties();
        url = getURLName(this.props);
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        if (a.data.imapSslType.equals("STARTTLS")) {
            props.setProperty("mail.imap.auth", "true");
            props.setProperty("mail.imap.timeout", TIMEOUT.toString());
            props.setProperty("mail.imap.connectiontimeout", TIMEOUT.toString());
            props.setProperty("mail.imap.socketFactory.class", "tech.overturn.crowmail.SSLCrowFactory");
            props.setProperty("ssl.SocketFactory.provider", "tech.overturn.crowmail.SSLCrowFactory");
            props.setProperty("mail.imap.socketFactory.port", a.data.imapPort.toString());
            props.setProperty("mail.imap.starttls.enable", "true");
        } else {
            props.setProperty("mail.imaps.timeout", TIMEOUT.toString());
            props.setProperty("mail.imaps.connectiontimeout", TIMEOUT.toString());
        }
        return props;
    }

    public URLName getURLName(Properties props) {
        String protocol = a.data.imapSslType.equals("STARTTLS") ? "imap" : "imaps";
        return new URLName(
                protocol,
                a.data.imapHost,
                a.data.imapPort,
                "Inbox",
                a.data.user,
                a.data.password);
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

    private void fetchMail() throws MessagingException {
        Long uidnext = getUidNext(folder, "Inbox");
        Log.d("fcrow", String.format("---- fetching next:%d", uidnext));
        Integer previous = (a.data.uidnext != null && a.data.uidnext != 0) ? a.data.uidnext : 1;
        if (previous != uidnext.intValue()) {
            Message[] msgs = folder.getMessagesByUID(previous, uidnext - 1);
            notifyUpdates(msgs);
            a.data.uidnext = uidnext.intValue();
            a.save(dbh.getWritableDatabase());
        }
        if(folder != null && folder.isOpen()) {
            folder.close(false);
        }
        if(store != null && store.isConnected()) {
            store.close();
        }
    }

    private void notifyUpdates(Message[] msgs) throws MessagingException {
        String msg_group_key;
        if (a.data._id != null) {
            msg_group_key = String.format("%s%d", Global.CROWMAIL, a.data._id);
        } else {
            msg_group_key = Global.CROWMAIL;
        }
        for (int i = 0; i < msgs.length; i++) {
            String from = msgs[i].getFrom()[0].toString();
            String subject = msgs[i].getSubject();
            Log.d("fcrow", String.format("---- email fetched:%s:%s", from, subject ));
            new CrowNotification(context).send(from, subject, msg_group_key, R.drawable.notif, false);
        }
    }

    public List<QItemReq> getReqs()
    {
       List<QItemReq> reqs = new ArrayList<QItemReq>();
       reqs.add(new QItemReqNetworkUp());
       return reqs;
    }

    public Runnable getTask() throws CrowmailException {
        ErrorStatus.fromStrings(context, dbh.getWritableDatabase(),
                "fetch fired"+a.data._id.toString(),
                CrowmailException.UNKNOWN.toString(),
                "",
                0,
                false
        );

        return new Runnable() {
            @Override
            public void run() {
                CrowmailException cme = null;
                Date startDebug = new Date();
                try {
                    connect();
                    fetchMail();
                } catch (Exception e) {
                    Throwable cause;
                    if ((cause = e.getCause()) != null
                            && cause instanceof ConnectionException
                            || cause instanceof SocketTimeoutException
                            || cause instanceof ConnectException
                            || cause instanceof SSLHandshakeException
                            || cause instanceof SocketException){
                        cme = new CrowmailException(CrowmailException.TIMEOUT, String.format("Connection error in fetch in:%d", startDebug.getTime()), e, a);
                    } else if (e instanceof MessagingException
                                || e instanceof ProtocolException
                                || e instanceof FolderClosedException) {
                        cme = new CrowmailException(CrowmailException.ERROR, "Server error in fetch.", e, a);
                    } else {
                        cme = new CrowmailException(CrowmailException.UNKNOWN, "Unknown error", e, a);
                    }
                    ErrorStatus.fromCme(context, dbh.getWritableDatabase(), "fetch:"+a.data._id.toString(), cme, true);
                    throw cme;
                }
            }
        };
    }

    public Long getDelay() {
        return FETCH_DELAY;
    }

    public String getAction() {
        return action;
    }

    private boolean updateFailureStats() {
        if(this.latestFailure == null) {
            this.latestFailure = new Date();
        }
        Date previous = this.latestFailure;
        this.latestFailure = new Date();
        if(this.latestFailure.getTime() - previous.getTime() > RELEASE_TIME){
            this.failCount = 0;
        }else {
            this.failCount++;
        }
        Log.d("fcrow", String.format("Fetcher.failCount:%d", this.failCount));
        return this.failCount <= MAX_ADJUSTED_FAIL;
    }

    public Long askRetry(CrowmailException e) {
        Log.d("fcrow", "--- ask retry");
        if(updateFailureStats()) {
            return this.getDelay();
        }
        ErrorStatus.fromCme(context, dbh.getWritableDatabase(), "too manny attempts:"+a.data._id.toString(), e, true);
        return -1L;
    }
}
