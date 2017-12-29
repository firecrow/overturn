package tech.overturn.crowmail.models;

import com.sun.mail.iap.ByteArray;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class CrowMessage {
    public String messageId;
    public InternetAddress from;
    public InternetAddress returnPath;
    public List<InternetAddress> to;
    public List<InternetAddress> cc;
    public Date date;
    public String subject;
    public String bodyText;
    public String bodyHtml;
    public Map<String, Date> relayDates;
    public List<ByteArray> attachments;
    public Boolean isIncoming;
}
