package tech.overturn.crowmail;

import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ModelBase {
    public static String tableName;
    public Map<String, View> ui;

    public ModelBase(){
        ui = new HashMap<String, View>();
    }

    public void setUI(String fname, View elem){
        ui.put(fname, elem);
    }
}


