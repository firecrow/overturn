package tech.overturn.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tech.overturn.R;
import tech.overturn.model.Ledger;

public class LedgerAdapter extends ArrayAdapter<Ledger> {
    Context context;
    int layoutId;
    List<Ledger> larray;

    public LedgerAdapter(Context context, int layoutId, List<Ledger> larray) {
        super(context, layoutId, larray);
        this.context = context;
        this.layoutId = layoutId;
        this.larray = larray;
    }

    @Override
    public long getItemId(int position) {
        if (larray.get(position) ==  null) {
            return 0;
        }
        return larray.get(position)._id;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        LedgerCont cont = null;
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);
            cont = new LedgerCont();
            cont.typeValue = (TextView)row.findViewById(R.id.ledgerTypeValue);
            cont.date = (TextView)row.findViewById(R.id.ledgerDate);
            cont.entity = (TextView)row.findViewById(R.id.ledgerEntity);
            cont.parent_id = (TextView)row.findViewById(R.id.ledgerParentId);
            row.setTag(cont);
        }else{
            cont = (LedgerCont)row.getTag();
        }
        Ledger ledger = larray.get(position);
        cont.entity.setText(ledger.entity);
        cont.parent_id.setText(ledger.parent_id.toString());
        cont.date.setText(ledger.date.toString());
        String value = null;
        if(ledger.longval != 0L){
            value = ledger.longval.toString();
        } else {
            value = ledger.strval;
        }
        cont.typeValue.setText(String.format("%d %s: %s",ledger._id, ledger.type, value));

        return row;
    }

    @Override
    public int getCount() {
        return larray.size();
    }

    public static class LedgerCont {
        TextView typeValue;
        TextView date;
        TextView entity;
        TextView parent_id;
    }
}
