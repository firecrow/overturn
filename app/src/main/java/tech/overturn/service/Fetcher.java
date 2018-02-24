package tech.overturn.service;

import android.content.Context;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Status;


import java.net.ConnectException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import tech.overturn.R;
import tech.overturn.model.Account;
import tech.overturn.model.Ledger;
import tech.overturn.util.CrowNotification;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

public class Fetcher {

    Account a;
    Context context;

    Properties props;
    URLName url;

    public static final Long TIMEOUT = 1000 * 5L;
    public static final Long FETCH_DELAY = 1000 * 15L;
    public static final Long FETCH_LIMIT = 300L;


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
            props.setProperty("mail.imap.socketFactory.class", "tech.overturn.SSLCrowFactory");
            props.setProperty("ssl.SocketFactory.provider", "tech.overturn.SSLCrowFactory");
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
                a.imapPort.intValue(),
                "Inbox",
                a.user,
                a.password);
    }

    private ImapState connect() throws MessagingException, ConnectException {
        if(!Global.hasNetwork(context)){
            throw new ConnectException();
        }
        Session session = Session.getInstance(props);
        Store store = session.getStore(url);
        store.connect();
        IMAPFolder folder = (IMAPFolder) store.getFolder(url);
        folder.open(Folder.READ_ONLY);
        return new ImapState(session, store, folder);
    }

    private Long getUidNext(IMAPFolder folder, final String folderName) throws MessagingException {
        return (Long) folder.doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               Status status = p.status(folderName, new String[]{"uidnext"});
               return status.uidnext;
            }
        });
    }

    private Long fetchMail(ImapState state) throws MessagingException {
        if(a.uidnext == null || a.uidnext == 0){
            a.uidnext = 1L;
        }
        Long previous = a.uidnext;
        Long uidnext = getUidNext(state.folder, "Inbox");
        if(uidnext - previous > FETCH_LIMIT){
            previous = uidnext - FETCH_LIMIT;
        }
        if (previous != uidnext) {
            Message[] msgs = state.folder.getMessagesByUID(previous, uidnext - 1);
            notifyUpdates(msgs);
            a.uidnext = uidnext;
            Orm.set(Global.getWriteDb(context),
                    Account.tableName, a._id, Ledger.UID_NEXT, new Date(), uidnext, null);
            Orm.insert(Global.getWriteDb(context),
                    Account.tableName, a._id, Ledger.MESSAGE_COUNT_TYPE, new Date(), Long.valueOf(msgs.length), null);
        }
        if(state.folder != null && state.folder.isOpen()) {
            state.folder.close(false);
        }
        if(state.store != null && state.store.isConnected()) {
            state.store.close();
        }
        return uidnext;
    }

    private void notifyUpdates(Message[] msgs) throws MessagingException {
        String msg_group_key;
        if (a._id != null) {
            msg_group_key = String.format("%s%d", Global.CROWMAIL, a._id);
        } else {
            msg_group_key = Global.CROWMAIL;
        }
        for (int i = 0; i < msgs.length; i++) {
            String from = msgs[i].getFrom()[0].toString();
            String subject = msgs[i].getSubject();
            new CrowNotification(context).send(
                    from, subject, msg_group_key, R.drawable.overturn_notif, false);
        }
    }

    public void loop(){
        Orm.set(Global.getWriteDb(context), 
            Account.tableName, a._id, Ledger.FETCH_TASK_CREATED, new Date(), null, null); 
        final Fetcher self = this;
        final Runnable runnable = new Runnable() {
            final Account _account = a;
            @Override
            public void run() {
                while (true) {
                    if(!Account.runStateForId(context, _account._id).equals(Ledger.RUNNING)){
                        Orm.set(Global.getWriteDb(context), 
                            Account.tableName, _account._id, Ledger.ACCOUNT_RUNNING_STATUS, new Date(), null, Ledger.STOPED);
                        break;
                    }
                    Long delay = Fetcher.FETCH_DELAY;
                    Date startDebug = new Date();
                    Long uidnext;
                    String exceptType = null;
                    Exception exc = null;
                    try {
                        uidnext = fetchMail(connect());
                        Orm.set(Global.getWriteDb(context), 
                            Account.tableName, _account._id, Ledger.LATEST_FETCH,
                            new Date(), new Date().getTime() - startDebug.getTime(), null);
                    } catch (ConnectException e) {
                        delay *= 5;
                        exceptType = Ledger.NETWORK_UNREACHABLE;
                        exc = e;
                    } catch (Exception e) {
                        delay *= 3;
                        exceptType = Ledger.ERROR;
                        exc = e;
                    }

                    if (exc != null) {
                        Orm.set(Global.getWriteDb(context), 
                            Account.tableName, _account._id, Ledger.MESSAGING_ERROR,
                            new Date(), new Date().getTime() - startDebug.getTime(), exceptType);
                    }

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Orm.set(Global.getWriteDb(context), 
                            Account.tableName, _account._id, Ledger.MESSAGING_ERROR,
                            new Date(), new Date().getTime() - startDebug.getTime(), Ledger.SLEEP_THREAD_INTERRUPTED);
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    public class ImapState {
        Session session;
        Store store;
        IMAPFolder folder;

        public ImapState(Session session, Store store, IMAPFolder folder) {
            this.session = session;
            this.store = store;
            this.folder = folder;
        }
    }
}
