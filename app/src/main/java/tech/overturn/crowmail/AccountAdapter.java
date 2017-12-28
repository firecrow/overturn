package tech.overturn.crowmail;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.AccountData;

public class AccountAdapter extends ArrayAdapter<AccountData> {
    Context context;
    int layoutId;
    List<AccountData> adarray;

    public AccountAdapter(Context context, int layoutId, List<AccountData> adarray) {
        super(context, layoutId, adarray);
        this.context = context;
        this.layoutId = layoutId;
        this.adarray = adarray;
    }

    @Override
    public long getItemId(int position) {
        if (adarray.get(position) ==  null) {
            return 0;
        }
        return adarray.get(position)._id;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        AccountCont cont = null;
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);
            cont = new AccountCont();
            cont.name = (TextView)row.findViewById(R.id.accountName);
            row.setTag(cont);
        }else{
            cont = (AccountCont)row.getTag();
        }
        cont.name.setText(adarray.get(position).imapHost);
        return row;
    }

    @Override
    public int getCount() {
        return adarray.size();
    }

    public static class AccountCont {
        TextView name;
    }
}
