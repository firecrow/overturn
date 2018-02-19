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
            cont.type = (TextView)row.findViewById(R.id.ledgerType);
            cont.date = (TextView)row.findViewById(R.id.ledgerDate);
            cont.longval = (TextView)row.findViewById(R.id.ledgerLongval);
            cont.textval = (TextView)row.findViewById(R.id.ledgerTextval);
            row.setTag(cont);
        }else{
            cont = (LedgerCont)row.getTag();
        }
        Ledger ledger = larray.get(position);
        cont.type.setText(ledger.type);
        cont.date.setText(ledger.date.toString());
        cont.longval.setText(ledger.longval.toString());
        cont.textval.setText(ledger.textval);
        return row;
    }

    @Override
    public int getCount() {
        return larray.size();
    }

    public static class LedgerCont {
        TextView date;
        TextView type;
        TextView label;
        TextView longval;
        TextView textval;
    }
}
