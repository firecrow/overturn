package tech.overturn.crowmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tech.overturn.crowmail.models.Ledger;

public class LedgerActivity extends AppCompatActivity {

    DBHelper dbh;
    Long account_id;
    ListView lview;
    LocalReceiver recv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        dbh = new DBHelper(getBaseContext());
        Intent intent = getIntent();
        this.account_id = intent.getLongExtra("account_id", 0);
        TextView blink = (TextView)findViewById(R.id.backLink);
        blink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToAccount(account_id);
            }
        });
        lview = (ListView) findViewById(R.id.ledgerList);
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToLedger(id, account_id);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        initLocalReciever();
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(recv);
    }

    private void refresh() {
        List<Ledger> ldata = (List<Ledger>) Orm.byQuery(dbh.getReadableDatabase(),
                Ledger.tableName,
                Ledger.class,
                "account_id = ?",
                new String[] {account_id.toString()},
                "date desc");
        Log.d("fcrow", String.format("in resume: %d %d", account_id, ldata.size()));

        LedgerAdapter adapter = (LedgerAdapter)lview.getAdapter();

        if(adapter == null) {
            lview.setAdapter(new LedgerAdapter(this, R.layout.ledger_row, ldata));
        } else {
            adapter.clear();
            adapter.addAll(ldata);
            adapter.notifyDataSetChanged();
        }
    }

    private void goToAccount(Long id) {
        Intent intent = new Intent(this, AccountActivity.class);
        Log.d("fcrow",String.format("go to ledger %d", id));
        intent.putExtra("account_id", id.longValue());
        startActivity(intent);
    }

    private void goToLedger(Long id, Long account_id) {
        Intent intent = new Intent(this, LedgerDetailActivity.class);
        intent.putExtra("ledger_id", id.longValue());
        intent.putExtra("account_id", account_id.longValue());
        startActivity(intent);
    }

    private void initLocalReciever() {
        recv = new LocalReceiver(this);
        registerReceiver(recv, new IntentFilter(Ledger.LEDGER_UPDATED));
    }

    private class LocalReceiver extends BroadcastReceiver {
        LedgerActivity activity;

        public LocalReceiver(LedgerActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            activity.refresh();
        }
    }
}
