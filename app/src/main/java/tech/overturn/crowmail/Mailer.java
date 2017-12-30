package tech.overturn.crowmail;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.activation.DataSource;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.CrowMessage;

public class Mailer extends javax.mail.Authenticator {
    Account account;
    Session session;

    public Mailer(Account account) {
        this.account = account;
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.host", account.data.smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", account.data.smtpPort);
        props.put("mail.smtp.socketFactory.port", account.data.smtpPort);
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
        Session session = Session.getDefaultInstance(props, this);
    }

    public void send(CrowMessage msg){
        try {
            MimeMessage mime = new MimeMessage(session);
            DataHandler handler = new DataHandler(new Handler(msg.bodyText.getBytes()));
            mime.setSender(msg.from);
            mime.setSubject(msg.subject);
            mime.setDataHandler(handler);
            mime.setRecipients(Message.RecipientType.TO, msg.to.toArray(new InternetAddress[msg.to.size()]));
            Transport.send(mime);
            Log.d("fcrow", "------------ just sent email -----");
        } catch(Exception e) {
            // TODO: figure out how to handle this
            Log.d("fcrow","------ error in Mailer.send ---"+e.getMessage(), e);
        }
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(account.data.user, account.data.password);
    }

    public static class Handler implements DataSource {

        byte[] bytes;

        public Handler(byte[] bytes) {
            this.bytes = bytes;
        }

        public String getName() {
            return "Handler";
        }

        public String getContentType() {
           return "text/plain";
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(bytes);
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
