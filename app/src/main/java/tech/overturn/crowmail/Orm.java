package tech.overturn.crowmail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Orm {

    public static Data objFromCursor(Cursor cursor, String[] cols, Class<Data> cls) {
        Data obj = null;
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

    public static void update(SQLiteDatabase db, String table, Data obj) {
        String className = obj.getClass().getName();
        ContentValues vals = new ContentValues();
        Field fields[] = obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getName().equals("serialVersionUID")) {
                continue;
            }
            try {
                Object value = f.get(obj);
                if(className.equals(f.getDeclaringClass().getName()) && value != null) {
                    Log.d("fcrow", String.format("------------>%s---%s vs %s", f.getName(), className, f.getDeclaringClass().getName()));
                    vals.put(f.getName(), value.toString());
                }
            } catch (IllegalAccessException e){
                // TODO: handle this better
            }
        }
        String[] idstr = { String.format("%d", obj._id)};
        if (vals.size() > 0) {
            db.update(table, vals, "_id = ?", idstr);
        }
    }

    public static void insert(SQLiteDatabase db, String table, Data obj) {
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
        if (vals.size() > 0) {
            obj._id = db.insertOrThrow(table, null, vals);
        }
    }

    public static String[] getSelectColumns(Class<Data> cls) {
        Field[] fields =  cls.getFields();
        String[] cols = new String[fields.length];
        for(int i = 0; i < fields.length; i++) {
            cols[i] = fields[i].getName();
        }
        return cols;
    }

    public static Data byId(SQLiteDatabase db, Class<Data> cls, Integer id) {
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
        Data obj =  objFromCursor(cursor, cols, cls);
        cursor.close();
        return obj;
    }

    public static List<Data> byQuery(SQLiteDatabase db, Class<Data> cls, String where, String order) {
        String[] cols = getSelectColumns(cls);
        List<Data> objs = new ArrayList<Data>();
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

    public static void backfillFromUI(Data obj, Map<String, View> ui) {
        Field[] fields =  obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String name = f.getName();
            String value = null;
            Class type = f.getType();
            View v = ui.get(name);
            if (v != null) {
                try {
                    if (v instanceof EditText) {
                        value = ((EditText) v).getText().toString();
                    } else if (v instanceof Spinner) {
                        value = ((Spinner) v).getSelectedItem().toString();
                    } else if (v instanceof RadioGroup) {
                        int id = ((RadioGroup)v).getCheckedRadioButtonId();
                        if(id != -1) {
                            value = ((RadioButton) v.findViewById(id)).getText().toString();
                        }
                    }
                    if(value != null) {
                        if (type.equals(String.class)) {
                            f.set(obj, value);
                        } else if (type.equals(Integer.class)) {
                            try {
                                f.set(obj, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                // TODO: better handle this;
                            }
                        }
                    }
                }catch(IllegalAccessException e){
                    // TODO: figure out what to do
                }
            }
        }
    }
}
