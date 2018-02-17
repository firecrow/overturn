package net.crowmail.service;

import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
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

import net.crowmail.model.Account;
import net.crowmail.model.CrowMessage;

public class Mailer extends javax.mail.Authenticator {
    Account a;
    Session session;

    public Mailer(Account a) {
        this.a = a;
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.host", a.smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", a.smtpPort.toString());
        props.put("mail.smtp.socketFactory.port", a.smtpPort.toString());
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
        session = Session.getDefaultInstance(props, this);
    }

    public void send(CrowMessage msg){
        try {
            MimeMessage mime = new MimeMessage(session);
            mime.setSender(msg.from);
            mime.setSubject(msg.subject);
            mime.setText(msg.bodyText);
            mime.setRecipients(Message.RecipientType.TO, msg.to);
            SMTPTransport trans = (SMTPTransport) session.getTransport("smtps");
            Log.d("fcrow", String.format("------------ opened transport %s/%s:%s -----", a.smtpHost, a.user, a.smtpPort.toString()));
            trans.connect(a.smtpHost, a.user, a.password);
            Log.d("fcrow", "------------ logged in transport -----");
            trans.sendMessage(mime, mime.getAllRecipients());
            Log.d("fcrow", "------------ sent in transport -----");
            trans.close();
            Log.d("fcrow", "------------ closed in transport -----");
            Log.d("fcrow", "------------ just sent email -----");
        } catch(Exception e) {
            // TODO: figure out how to handle this
            Log.d("fcrow","------ error in Mailer.send ---"+e.getMessage(), e);
        }
    }
}
