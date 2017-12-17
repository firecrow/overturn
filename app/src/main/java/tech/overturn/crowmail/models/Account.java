package tech.overturn.crowmail.models;

import android.util.Log;

import java.lang.reflect.Field;

import tech.overturn.crowmail.FieldMetaInteger;
import tech.overturn.crowmail.FieldMetaString;
import tech.overturn.crowmail.ModelIfc;

public class Account extends ModelIfc {
    public FieldMetaString name;
    public FieldMetaString user;
    public FieldMetaString password;
    public FieldMetaString smtpHost;
    public FieldMetaInteger smtpPort;
    public FieldMetaString smtpSslType;
    public FieldMetaString imapHost;
    public FieldMetaInteger imapPort;
    public FieldMetaString imapSslType;

    public Account(){
        super();
    }
}
