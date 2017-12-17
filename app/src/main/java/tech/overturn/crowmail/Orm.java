package tech.overturn.crowmail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Orm {

    public static ModelIfc objFromCursor(Cursor cursor, String[] cols, Class<ModelIfc> cls) {
        ModelIfc obj = null;
        try {
            obj = cls.newInstance();
        }catch (InstantiationException e) {
            // TODO: handle this better
        }catch (IllegalAccessException e) {
            // TODO: handle this better
        };
        int idx;
        Field fields[] = cls.getFields();
        List<String> colsList = Arrays.asList(cols);
        for (int i = 0; i < fields.length; i++) {
            try {
                Field f = fields[i];
                idx = colsList.indexOf(f.getName().toLowerCase());
                if (idx == -1) {
                    continue;
                }
                if (f.getType().equals(Integer.class)) {
                    f.set(obj, cursor.getInt(idx));
                } else if (f.getType().equals(String.class)) {
                    f.set(obj, cursor.getString(idx));
                }
            } catch (IllegalAccessException e){
                // TODO: figure out how to handle this better
            }
        }
        return obj;
    }

    public static String getCreateTable(Class cls) {
        String query = "";
        String table = cls.getSimpleName().toLowerCase();
        query += "CREATE TABLE "+table +" ( ";
        query += "_id INTEGER PRIMARY KEY AUTOINCREMENT";
        Field fields[] = cls.getFields();
        Log.d("fcrow", String.format("%d",fields.length));
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if(Modifier.isStatic(f.getModifiers()) || f.getName() == "_id"){
                continue;
            }
            query += ", "+ f.getName().toLowerCase();
            if(f.getType().equals(Integer.class)) {
                query += " INTEGER ";
            }else if(f.getType().equals(String.class)) {
                query += " TEXT ";
            }
        }
        query += " );";
        return query;
    }

    public static void update(SQLiteDatabase db, ModelIfc obj) {
        String table = obj.getClass().getSimpleName().toLowerCase();
        ContentValues vals = new ContentValues();
        Field fields[] = obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                vals.put(fields[i].getName(), fields[i].get(obj).toString());
            } catch (IllegalAccessException e){
                // TODO: handle this better
            }
        }
        String[] idstr = { String.format("%d", obj._id)};
        db.update(table, vals, "_id = ?", idstr);
    }

    public static void insert(SQLiteDatabase db, ModelIfc obj) {
        String table = obj.getClass().getSimpleName().toLowerCase();
        ContentValues vals = new ContentValues();
        Field fields[] = obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            String fname = fields[i].getName();
            Field f = fields[i];
            try {
                if(fname != "_id" && fname != "serialVersionUID" && f.get(obj) != null) {
                    if(f.getType() == Integer.class) {
                        Integer value = (Integer) f.get(obj);
                        if(value != null) {
                            vals.put(fname, value.toString());
                        }
                    }
                    else if(f.getType() == String.class) {
                        String value = (String) f.get(obj);
                        if(value != null) {
                            vals.put(fname, value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                // TODO: handle this better
                Log.e("fcrow", "error in insert illegal");
            }
        }
        obj._id = db.insertOrThrow(table, null, vals);
    }

    public static void upsert(SQLiteDatabase db, ModelIfc model) {
        if(model._id != 0){
            update(db, model);
        }else{
            insert(db, model);
        }
    }

    public static String[] getSelectColumns(Class<ModelIfc> cls) {
        Field[] fields =  cls.getFields();
        String[] cols = new String[fields.length];
        for(int i = 0; i < fields.length; i++) {
            cols[i] = fields[i].getName();
        }
        return cols;
    }

    public static ModelIfc byId(SQLiteDatabase db, Class<ModelIfc> cls, Integer id) {
        String[] cols = getSelectColumns(cls);
        String[] idstr = { id.toString(0)};
        Cursor cursor = db.query(
            cls.getSimpleName().toLowerCase(),
            cols,
            "_id = ?",
            idstr,
            null,
            null,
            null
        );
        cursor.moveToNext();
        ModelIfc obj =  objFromCursor(cursor, cols, cls);
        cursor.close();
        return obj;
    }

    public static List<ModelIfc> byQuery(SQLiteDatabase db, Class<ModelIfc> cls, String where, String order) {
        String[] cols = getSelectColumns(cls);
        List<ModelIfc> objs = new ArrayList<ModelIfc>();
        Cursor cursor = db.query(
                cls.getSimpleName().toLowerCase(),
                cols,
                null,
                null,
                null,
                null,
                null
        );
        while(cursor.moveToNext()) {
            objs.add(objFromCursor(cursor, cols, cls));
        }
        cursor.close();
        return objs;
    }
}
