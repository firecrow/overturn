package net.crowmail.model;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.RecyclerView;

import net.crowmail.util.DbField;

public class AccountData extends Data {

    @DbField
    public String name;

    @DbField
    public String email;

    @DbField
    public String user;

    @DbField
    public String password;

    @DbField
    public String smtpHost;

    @DbField
    public Integer smtpPort;

    @DbField
    public String smtpSslType;

    @DbField
    public String imapHost;

    @DbField
    public Integer imapPort;

    @DbField
    public String imapSslType;

    @DbField
    public Integer uidnext;
}
