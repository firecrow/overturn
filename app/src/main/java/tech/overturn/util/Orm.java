package tech.overturn.util;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import tech.overturn.model.Data;
import tech.overturn.model.Ledger;

import static java.lang.Math.toIntExact;


public class Orm {

    static String[] fields = new String[]{"_id", "entity_id", "date", "entity", "type", "longval", "strval"};

    public static Ledger ledgerFromCursor(Cursor cursor) {
        Ledger ledger = new Ledger(
            cursor.getLong(1),
            cursor.getString(2),
            new Date(cursor.getLong(3)),
            cursor.getString(4),
            cursor.getLong(5),
            cursor.getString(6)
        );
        ledger._id = cursor.getLong(0);
    }

    public static String getCreateTable() {
        return  ""
                + "CREATE TABLE ledger ( "
                + "  _id INTEGER PRIMARY_KEY_AUTOINCREMENT "
                + "  entity_id INTEGER, "
                + "  date INTEGER, "
                + "  entity TEXT, "
                + "  type TEXT, "
                + "  longval INTEGER, "
                + "  strval TEXT "
                + ");"
                ;

    private static Long insertMaster(SQLiteDatabase db, String entity) {
        ContentValues vals = new ContentValues();
        vals.put("entity", entity);
        return db.insertOrThrow(Ledger.SCHEMA, null, vals);
    }

    public static void upsert(SQLiteDatabase db, Data obj) {
        db.beginTransaction();
        if(obj._id == null || obj._id == 0 || getMaster(db, obj._id, obj._entity) == null) {
            obj._id = Orm.insertMaster(db, obj._entity);
        }
        try {
            for (Field f : obj.getClass().getFields()) {
                if (f.isAnnotationPresent(DbField.class)) {
                    if(f.get(obj) == null) {
                        unsetLedger(db, obj._id, obj._entity, f.getName());
                        continue;
                    }
                    Ledger ledger = new Ledger();
                    ledger.entity = obj._entity;
                    ledger.entity_id = obj._id;
                    ledger.type = f.getName();
                    if (f.getType() == Long.class) {
                        ledger.longval = (Long) f.get(obj);
                    } else if (f.getType() == Date.class) {
                        ledger.longval = ((Date)f.get(obj)).getTime();
                    } else if (f.getType() == String.class) {
                        ledger.strval = (String) f.get(obj);
                    }
                    ledger.save(db);
                }
            }
        } catch(Exception e){}
        db.endTransaction();
    }

    public static Data byId(SQLiteDatabase db, Class<? extends Data> cls, String entity, Long entity_id) {
        Ledger master = ledgersById(db, entity, entity_id);
        Data obj = null;
        try {
            obj = cls.newInstance();
            obj._id = master._id;
            obj._entity = entity;
            for (Ledger item: master.children) {
                Field f = cls.getField(item.type);
                if(f.getType() == Date.class) {
                    f.set(obj, new Date(item.longval));
                }else if(f.getType() == Long.class) {
                    f.set(obj, item.longval);
                }else if(f.getType() == String.class) {
                    f.set(obj, item.strval);
                }
            }
            return obj;
        } catch (Exception e) {}
    }

    public static Ledger getMaster(SQLiteDatabase db, Long entity_id, String entity) {
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                "_id = ? and entity = ?",
                new String[]{entity_id.toString(), entity},
                null,
                null,
                "date desc",
                "1"
        );
        cursor.moveToNext();
        Ledger master = null;
        if(cursor.getCount() == 1) {
            master = ledgerFromCursor (cursor);
        }
        cursor.close();
        return master;
    }

    public static Ledger ledgersById(SQLiteDatabase db, Long entity_id, String entity) {
        Ledger master = getMaster(db, entity_id, entity);
        if(master == null) {
            return null;
        }
        Cursor cursor = db.query(
            Ledger.SCHEMA,
            fields,
            "entity_id = ? and entity = ?",
            new String[]{entity_id.toString(), entity},
            null,
            null,
            "date desc"
        );
        while (cursor.moveToNext()) {
            master.children.add(ledgerFromCursor(cursor));
        }
        return master;
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
            }catch(IllegalAccessException e){}
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

    public static void unsetLedger(SQLiteDatabase db, Long entity_id, String entity, String type) {
        db.delete(Ledger.SCHEMA, "entity_id = ? and entity = ? and type = ?",
                new String[]{entity_id.toString(), entity, type});
    }
}
