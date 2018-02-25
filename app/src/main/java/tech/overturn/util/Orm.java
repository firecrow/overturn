package tech.overturn.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
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
import tech.overturn.model.Id;
import tech.overturn.model.Ledger;

import static java.lang.Math.toIntExact;


public class Orm {

    static String[] fields = new String[]{"_id", "parent_id", "entity", "date", "type", "longval", "strval"};

    public static Ledger ledgerFromCursor(Cursor cursor) {
        Log.d("fcrow", String.format("--------------- date:%d", cursor.getLong(3)));
        Ledger ledger = new Ledger(
            cursor.getLong(1),
            cursor.getString(2),
            new Date(cursor.getLong(3)),
            cursor.getString(4),
            cursor.getLong(5),
            cursor.getString(6)
        );
        ledger._id = cursor.getLong(0);
        return ledger;
    }

    public static List<Ledger> recentLedgers(SQLiteDatabase db, Long since) {
        Log.d("fcrow", "----------- recentLedgers called");
        List<Ledger> ledgers = new ArrayList<Ledger>();
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                "date > ?" ,
                new String[]{new Long(new Date().getTime() - since).toString()},
                null,
                null,
                "date desc"
        );
        Log.d("fcrow", "----------- after query recentLedgers called");
        while(cursor.moveToNext()){
            Log.d("fcrow", "----------- next");
            Ledger l = ledgerFromCursor(cursor);
            Log.d("fcrow", "----------- next: "+l.date.toString());
            ledgers.add(ledgerFromCursor(cursor));
        }
        cursor.close();
        Log.d("fcrow", "----------- returning");
        return ledgers;
    }

    public static String getCreateTable() {
        return ""
                + "CREATE TABLE ledger ( "
                + "  _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "  parent_id INTEGER, "
                + "  date INTEGER, "
                + "  entity TEXT, "
                + "  type TEXT, "
                + "  longval INTEGER, "
                + "  strval TEXT "
                + ");"
                ;
    }

    private static Long insertMaster(SQLiteDatabase db, String entity) {
        ContentValues vals = new ContentValues();
        vals.put("entity", entity);
        vals.put("parent_id", 0);
        vals.put("date", new Date().getTime());
        /*
        Intent intent = new Intent(Ledger.LEDGER_UPDATED);
        context.sendBroadcast(intent);
        */
        return db.insertOrThrow(Ledger.SCHEMA, null, vals);
    }

    public static void upsert(SQLiteDatabase db, Data obj) {
        Log.d("fcrow", "------------ upsert called");
        db.beginTransaction();
        if(obj._id == null || obj._id == 0 || getMaster(db, obj._id, obj._entity) == null) {
            obj._id = Orm.insertMaster(db, obj._entity);
        }else {
            bumpDate(db, obj._id);
        }
        try {
            for (Field f : obj.getClass().getFields()) {
                if (f.isAnnotationPresent(DbField.class)) {
                    if(f.get(obj) == null) {
                        unsetLedger(db, obj._id, obj._entity, f.getName());
                        continue;
                    }
                    Ledger ledger = new Ledger();
                    ledger.type = f.getName();
                    if(ledger.type.equals("_id") || ledger.type.equals("_entity")){
                        continue;
                    }
                    ledger.date = new Date();
                    ledger.entity = obj._entity;
                    ledger.parent_id = obj._id;
                    Log.d("fcrow", String.format("------------ parent:%d, entity:%s, type:%s %s", obj._id, obj._entity, f.getName(), f.getType().getName()));
                    ;
                    if (f.getType() == Long.class) {
                        ledger.longval = (Long) f.get(obj);
                        Log.d("fcrow", String.format("------------ long: %d", ledger.longval));
                    } else if (f.getType() == Date.class) {
                        ledger.longval = ((Date)f.get(obj)).getTime();
                    } else if (f.getType() == String.class) {
                        ledger.strval = (String) f.get(obj);
                    }
                    set(db, ledger);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e){
            Log.d("fcrow", "------------ upsert called"+e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public static Data byId(SQLiteDatabase db, Class<? extends Data> cls, String entity, Long parent_id) {
        Ledger master = ledgersById(db, parent_id, entity);
        Log.d("fcrow", String.format("master is? %b ", master == null));
        Data obj = null;
        try {
            obj = cls.newInstance();
            obj._id = master._id;
            obj._entity = entity;
            for (Ledger item: master.children) {
                try {
                    Field f = cls.getField(item.type);
                    if (f.getType() == Date.class) {
                        f.set(obj, new Date(item.longval));
                    } else
                        if (f.getType() == Long.class) {
                            f.set(obj, item.longval);
                        } else
                            if (f.getType() == String.class) {
                                f.set(obj, item.strval);
                            }
                } catch (NoSuchFieldException e){}
            }
        } catch (Exception e) {
            Log.d("fcrow", "---------------- byId did not go so well: " + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    public static  List<Id> idsByEntity(SQLiteDatabase db, String entity) {
        List<Id> ids = new ArrayList<Id>();
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                new String[]{"_id"},
                "entity = ? and parent_id = 0",
                new String[]{entity},
                null,
                null,
                "date desc"
        );
        while (cursor.moveToNext()) {
            ids.add(new Id(cursor.getLong(0)));
        }
        cursor.close();
        return ids;
    }

    public static List<? extends Data> byEntity(SQLiteDatabase db, Class<? extends Data> cls, String entity) {
        List<Id> ids = idsByEntity(db, entity);
        List<Data> list = new ArrayList<Data>();
        for(Id id : ids) {
            list.add(Orm.byId(db, cls, entity, id._id));
        }
        return list;
    }

    public static Ledger getAttribute(SQLiteDatabase db, String type, Long parent_id, String entity) {
        Ledger att = null;
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                String.format("type = ? AND entity = ? AND parent_id = ?"),
                new String[]{type, entity, parent_id.toString()},
                null,
                null,
                "date desc",
                "1"
        );
        cursor.moveToNext();
        if(cursor.getCount() == 1) {
            att = ledgerFromCursor (cursor);
        }
        cursor.close();
        return att;
    }

    public static void set(SQLiteDatabase db, Ledger ledger) {
        if (ledger.entity == null || ledger.parent_id == null || ledger.type == null) {
            throw new IllegalArgumentException("for 'set' all three of 'entity', 'parent_id', and 'type' need to be not null");
        }

        Ledger existing = get(db, null, ledger.entity, ledger.parent_id, ledger.type, null,null);
        if(existing == null) {
            insert(db, ledger.entity, ledger.parent_id, ledger.type, ledger.date, ledger.longval, ledger.strval);
        }else {
            update(db, existing._id, ledger.entity, ledger.parent_id, ledger.type, ledger.date, ledger.longval, ledger.strval);
        }
    }


    public static void set(SQLiteDatabase db,
            String entity, Long parent_id, String type, Date date, Long longval, String strval) {
        if (entity == null || parent_id == null || type == null) {
            throw new IllegalArgumentException("for 'set' all three of 'entity', 'parent_id', and 'type' need to be not null");
        }

        Ledger existing = get(db, null, entity, parent_id, type, null,null);
        if(existing == null) {
            insert(db, entity, parent_id, type, date, longval, strval);
        }else {
            update(db, existing._id, entity, parent_id, type, date, longval, strval);
        }
    }

    public static void setById(SQLiteDatabase db,
            Long id, String entity, Long parent_id, String type, Date date, Long longval, String strval) {
        Ledger existing = get(db, id, null, null, null, null,null);
        if(existing != null) {
            update(db, existing._id, entity, parent_id, type, date, longval, strval);
        }else {
            throw new IllegalArgumentException("no ledger ith that _id found");
        }
    }

    public static Ledger get(SQLiteDatabase db, Long id) {
        return get(db, id, null, null, null, null, null);
    }

    public static Ledger get(SQLiteDatabase db,
                Long id, String entity, Long parent_id, String type, Long longval, String strval) {
        List<String> values = new ArrayList<String>();
        List<String> columns = new ArrayList<String>();
        Ledger ledger = null;

        if (id != null) {
            columns.add("_id = ?");
            values.add(id.toString());
        }
        if (entity != null) {
            columns.add("entity = ?");
            values.add(entity);
        }
        if (parent_id != null) {
            columns.add("parent_id = ?");
            values.add(parent_id.toString());
        }
        if (type != null) {
            columns.add("type = ?");
            values.add(type);
        }
        if (longval != null) {
            columns.add("longval = ?");
            values.add(longval.toString());
        }
        if(strval != null) {
            columns.add("strval = ?");
            values.add(strval);
        }

        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                TextUtils.join(" and ", columns),
                values.toArray(new String[values.size()]),
                null,
                null,
                "date desc",
                "1"
        );
        cursor.moveToNext();
        if(cursor.getCount() == 1) {
            ledger = ledgerFromCursor(cursor);
        }
        cursor.close();
        return ledger;
    }

    public static Ledger getLedgerById(SQLiteDatabase db, Long id) {
        Ledger ledger = null;
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                String.format("_id = ?"),
                new String[]{id.toString()},
                null,
                null,
                null
        );
        cursor.moveToNext();
        if(cursor.getCount() == 1) {
            ledger = ledgerFromCursor(cursor);
        }
        cursor.close();
        return ledger;
    }

    public static Long insert(SQLiteDatabase db,
            String entity, Long parent_id, String type, Date date, Long longval, String strval) {
        Ledger ledger = null;

        ContentValues vals = new ContentValues();
        if(entity != null)
            vals.put("entity", entity);
        if(parent_id != null)
            vals.put("parent_id", parent_id);
        if(type != null)
            vals.put("type", type);
        if(date != null)
            vals.put("date", date.getTime());
        if(longval != null)
            vals.put("longval", longval);
        if(strval != null)
            vals.put("strval", strval);

        /*
        Intent intent = new Intent(Ledger.LEDGER_UPDATED);
        context.sendBroadcast(intent);
        */
        return db.insertOrThrow(Ledger.SCHEMA, null, vals);
    }

    public static void bumpDate(SQLiteDatabase db, Long id){
        ContentValues vals = new ContentValues();
        vals.put("date", new Date().getTime());
        db.update(Ledger.SCHEMA, vals, "_id=?", new String[]{id.toString()});
    }

    public static void update(SQLiteDatabase db,
            Long id, String entity, Long parent_id, String type, Date date, Long longval, String strval) {
        Ledger ledger = null;

        ContentValues vals = new ContentValues();
        if(entity != null)
            vals.put("entity", entity);
        if(parent_id != null)
            vals.put("parent_id", parent_id);
        if(type != null)
            vals.put("type", type);
        if(date != null)
            vals.put("date", date.getTime());
        if(longval != null)
            vals.put("longval", longval);
        if(strval != null)
            vals.put("strval", strval);

        /*
        Intent intent = new Intent(Ledger.LEDGER_UPDATED);
        context.sendBroadcast(intent);
        */
        db.update(Ledger.SCHEMA, vals, "_id=?", new String[]{id.toString()});
    }

    public static Ledger getMaster(SQLiteDatabase db, Long parent_id, String entity) {
        Cursor cursor = db.query(
                Ledger.SCHEMA,
                fields,
                "_id = ? and entity = ?",
                new String[]{parent_id.toString(), entity},
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

    public static Ledger ledgersById(SQLiteDatabase db, Long parent_id, String entity) {
        Ledger master = getMaster(db, parent_id, entity);
        if(master == null) {
            return null;
        }
        Cursor cursor = db.query(
            Ledger.SCHEMA,
            fields,
            "parent_id = ? and entity = ?",
            new String[]{parent_id.toString(), entity},
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
                        } else if (type.equals(Long.class)) {
                            f.set(obj, Long.valueOf(value));
                        }
                    }
                }catch(IllegalAccessException e){
                    // TODO: figure out what to do
                }
            }
        }
    }

    public static void unsetLedger(SQLiteDatabase db, Long parent_id, String entity, String type) {
        db.delete(Ledger.SCHEMA, "parent_id = ? and entity = ? and type = ?",
                new String[]{parent_id.toString(), entity, type});
    }
}
