package tech.overturn.crowmail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class Orm {
    private SQLiteDatabase db;
    Orm(SQLiteDatabase db) {
        this.db = db;
    }

    /*
    public static ModelIfc objFromCursor(Class cls, Cursor cursor) {
        cls obj = new cls();
        Field fields[] = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            switch (f.getType()) {
                case Integer:
                    f.set(obj, cursor.getInt());
                    break;
                case String:
                    f.set(obj, cursor.getString());
                    break;
            }
        }
        return obj;
    }
    */

    public static String genCreateTable(Class cls) {
        String query = "";
        String table = cls.getName().toLowerCase().replaceAll("^.*\\.", "");
        query += "CREATE TABLE "+table +" ( ";
        query += "_id INT PRIMARY KEY ";
        Field fields[] = cls.getFields();
        Log.d("fcrow", String.format("%d",fields.length));
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if(Modifier.isStatic(f.getModifiers()) || f.getName() == "_id"){
                continue;
            }
            query += ", "+ f.getName().toLowerCase();
            if(f.getType().equals(Integer.class)) {
                query += " INT ";
            }else if(f.getType().equals(String.class)) {
                query += " TEXT ";
            }
        }
        query += " );";
        return query;
    }
}
