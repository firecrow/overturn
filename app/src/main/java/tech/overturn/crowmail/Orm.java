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
import static java.lang.Math.toIntExact;


public class Orm {

    public static Data objFromCursor(Cursor cursor, String[] cols, Class<? extends Data> cls) {
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
                String name = f.getName();
                idx = colsList.indexOf(name);
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

    public static String getCreateTable(String table, Class cls) {
        String query = "";
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
            if (Modifier.isStatic(f.getModifiers()) || f.getName().equals("_id") || f.getName().equals("serialVersionUID")) {
                continue;
            }
            try {
                Object value = f.get(obj);
                String declaring = f.getDeclaringClass().getName();
                if(className.equals(declaring) && value != null) {
                    vals.put(f.getName(), value.toString());
                }
            } catch (IllegalAccessException e){
                // TODO: handle this better
            }
        }
        if (vals.size() > 0) {
            String where = String.format("_id=%d", obj.getId());
            int res = db.update(table, vals, where, null);
            Log.d("fcrow", String.format("--- update res:%d '%s'", res, where));
        }
    }

    public static void insert(SQLiteDatabase db, String table, Data obj) {
        ContentValues vals = new ContentValues();
        Field fields[] = obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            String fname = fields[i].getName();
            Field f = fields[i];
            try {
                if(!Modifier.isStatic(f.getModifiers()) && fname != "_id" && fname != "serialVersionUID" && f.get(obj) != null) {
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
            obj.setId(toIntExact(db.insertOrThrow(table, null, vals)));
        }
    }

    public static String[] getSelectColumns(Class<? extends Data> cls) {
        Field[] fields =  cls.getFields();
        String[] cols = new String[fields.length+1];
        for(int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers()) || fields[i].getName().equals("serialVersionUID")) {
                continue;
            }
            cols[i] = fields[i].getName();
        }
        return cols;
    }

    public static Data byId(SQLiteDatabase db, String table, Class<? extends Data> cls, Integer id) {
        String[] cols = getSelectColumns(cls);
        Cursor cursor = db.query(
            table,
            cols,
            String.format("_id = %d", id),
            null,
            null,
            null,
            null
        );
        Data obj = null;
        int count = cursor.getCount();
        if(count == 1 && cursor.moveToNext()) {
            obj = objFromCursor(cursor, cols, cls);
        } else {
            // TODO: handle this error
        }
        cursor.close();
        return obj;
    }

    public static List<? extends Data> byQuery(SQLiteDatabase db, String table, Class<? extends Data> cls, String where, String order) {
        String[] cols = getSelectColumns(cls);
        List<Data> objs = new ArrayList<Data>();
        Cursor cursor = db.query(
                table,
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

    public static List<? extends Data> byQueryRaw(SQLiteDatabase db, Class<? extends Data> cls, String qry, String[] args) {
        String[] cols = getSelectColumns(cls);
        List<Data> objs = new ArrayList<Data>();
        Cursor cursor = db.rawQuery(qry, args);
        while(cursor.moveToNext()) {
            objs.add(objFromCursor(cursor, cols, cls));
        }
        cursor.close();
        return objs;
    }

    public static void fillUI(Data obj, Map<String, View> ui) {
        Field[] fields =  obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String name = f.getName();
            String value = null;
            Class type = f.getType();
            View v = ui.get(name);
            try {
                if (v != null && f.get(obj) != null) {
                    value  = f.get(obj).toString();
                    if (v instanceof EditText) {
                        ((EditText) v).setText(value);
                    } else if (v instanceof Spinner) {
                        value = ((Spinner) v).getSelectedItem().toString();
                    } else if (v instanceof RadioGroup) {
                        RadioGroup rdg = (RadioGroup)v;
                        RadioButton btn;
                        int length = rdg.getChildCount();
                        for (int j = 0; j < length; j++) {
                            btn = (RadioButton) rdg.getChildAt(j);
                            if (btn.getText().equals(value)) {
                                btn.setChecked(true);
                            }
                        }
                    }
                }
            }catch(IllegalAccessException e){
                // TODO: figure out what to do
            }
        }
    }

    public static void backfillFromUI(Data obj, Map<String, View> ui) {
        Field[] fields =  obj.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String name = f.getName();
            String value = null;
            if (name.equals("_id")) {
                continue;
            }
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
