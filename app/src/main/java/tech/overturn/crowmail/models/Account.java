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
        Log.d("fcrow", "--------------- account called");
        String fname;
        Field fields[] = getClass().getFields();
        for(int i = 0; i < fields.length; i++){
            fname = fields[i].getName();
            if(fname != "_id" && fname != "serialVersionUID") {
                Field f = fields[i];
                Log.d("fcrow", "-----------------fieldName:"+f.getName());
                try {
                    if (f.getType().equals(FieldMetaInteger.class)) {
                        fields[i].set(this, new FieldMetaInteger());
                    } else if (f.getType().equals(FieldMetaString.class)) {
                        fields[i].set(this, new FieldMetaString());
                    }
                }catch(IllegalAccessException e){};
            }
        }
    }
}
