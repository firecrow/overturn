package tech.overturn.crowmail;

import android.util.Log;

import java.lang.reflect.Field;

public class ModelIfc {
    public long _id = 0;

    public ModelIfc(){
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


