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
import java.util.Arrays;
import java.util.List;


public class Orm extends SQLiteOpenHelper {

    public static ModelIfc objFromCursor(Cursor cursor, List<String> cols, Class cls) {
        cls obj = cls.newInstance();
        int idx;
        Field fields[] = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int idx = cols.indexOf(f.getName().toLowerCase());
            if(idx == -1) {
                continue;
            }
            switch (f.getType()) {
                case Integer:
                    f.set(obj, cursor.getInt(idx));
                    break;
                case String:
                    f.set(obj, cursor.getString(idx));
                    break;
            }
        }
        return obj;
    }

    public static String getCreateTable(Class cls) {
        String query = "";
        String table = cls.getSimpleName().toLowerCase();
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

    public static void update(SQLiteDatabase db, ModelIfc obj) {
        String table = obj.class.getSimpleName().toLowerCase();
        ContentValues vals = new ContentValues();
        Field fields[] = obj.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            vals.put(fields[i].getName(), fields[i].get(obj).toString());
        }
        db.update(table, vals, "_id = ?", String[]{obj._id.toString()});
    }

    public static void insert(SQLiteDatabase db, ModelIfc obj) {
        String table = obj.class.getSimpleName().toLowerCase();
        ContentValues vals = new ContentValues();
        Field fields[] = obj.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            vals.put(fields[i].getName(), fields[i].get(obj).toString());
        }
        obj._id = db.insert(table, null, vals);
    }

    public static void upsert(SQLiteDatabase db, ModelIfc model) {
        if(model._id){
            this.update(db, model);
        }else{
            this.insert(db, model);
        }
    }

    public static List<String> getSelectColumns(Class<ModelIfc> cls) {
        List<Field> cols = Arrays.asList(cls.getFields());
        return cols.stream().map(x -> x.getName());
    }

    public static ModelIfc byId(SQLiteDatabase db, Class<ModelIfc> cls, Integer id) {
        List<String> cols = this.getSelectColumns(cls);
        SQLiteCursor cursor = db.query(
            cls.getSimpleName().toLowerCase(),
            cols,
            "_id = ?",
            String[]{ id.toString(0)}
            null,
            null,
            null
        )
        cursor.moveToNext(0);
        ModelIfc obj =  this.objFromCursor(cursor, cols, cls);
        cursor.close();
        return obj;
    }

    public static List<ModelIfc> byQuery(Class<ModelIfc> cls, String where, String order) {
        List<String> cols = this.getSelectColumns(cls);
        List<ModelIfc> objs = new List<ModelIfc>();
        SQLiteCursor cursor = db.query(
                cls.getSimpleName().toLowerCase(),
                cols,
                null,
                null,
                null,
                null,
                null
        );
        while(cursor.moveToNext()) {
            objs.add(this.objFromCursor(cursor, cols, cls));
        }
        cursor.close();
        return objs;
    }
}
