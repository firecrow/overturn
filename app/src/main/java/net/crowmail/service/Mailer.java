package net.crowmail.service;

import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

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
            trans.connect(a.smtpHost, a.user, a.password);
            trans.sendMessage(mime, mime.getAllRecipients());
            trans.close();
        } catch(Exception e) {
            // TODO: figure out how to handle this
            Log.d("fcrow","------ error in Mailer.send ---"+e.getMessage(), e);
        }
    }
}
