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

public class Fetcher {

    Account a;
    Context context;

    Properties props;
    URLName url;

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
                a.imapPort,
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
        Long uidnext = getUidNext(state.folder, "Inbox");
        Integer previous = (a.uidnext != null && a.uidnext != 0) ? a.uidnext : 1;
        if (previous != uidnext.intValue()) {
            Message[] msgs = state.folder.getMessagesByUID(previous, uidnext - 1);
            notifyUpdates(msgs);
            a.uidnext = uidnext.intValue();
            a.save(Global.getWriteDb(context));
            new Ledger(
                    a._id,
                    Account.tableName,
                    new Date(),
                    Ledger.MESSAGE_COUNT_TYPE,
                    String.format("messages %d..%d", previous, uidnext),
                    Long.valueOf(msgs.length),
                    null
            ).log(Global.getWriteDb(context), context);
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
                    from, subject, msg_group_key, R.drawable.notif, false);
        }
    }

    public void loop(){
        new Ledger(
                a._id,
                Account.tableName,
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
                    if(!Account.isRunningById(context, _account._id)){
                        break;
                    }
                    Long delay = Fetcher.FETCH_DELAY;
                    Date startDebug = new Date();
                    Long uidnext;
                    String exceptType = null;
                    Exception exc = null;
                    try {
                        uidnext = fetchMail(connect());
                        new Ledger(
                                _account._id,
                                Account.tableName,
                                new Date(),
                                Ledger.UID_NEXT,
                                String.format("duration %d",
                                        new Date().getTime() - startDebug.getTime()),
                                uidnext,
                                null
                        ).log(Global.getWriteDb(context), context);
                    } catch (ConnectException e) {
                        delay *= 5;
                        exceptType = Ledger.NETWORK_UNREACHABLE;
                        exc = e;
                    } catch (Exception e) {
                        delay *= 3;
                        exceptType = Ledger.MESSAGING_ERROR;
                        exc = e;
                    }

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        exceptType = Ledger.SLEEP_THREAD_INTERRUPTED;
                        exc = e;
                    }

                    if(exc != null) {
                        new Ledger(
                                _account._id,
                                Account.tableName,
                                new Date(),
                                exceptType,
                                _account.imapHost,
                                new Date().getTime() - startDebug.getTime(),
                                Global.stackToString(exc)
                        ).log(Global.getWriteDb(context), context);
                    }

                    if(exceptType.equals(Ledger.SLEEP_THREAD_INTERRUPTED)) {
                        break;
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
