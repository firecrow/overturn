package tech.overturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tech.overturn.adapters.LedgerAdapter;
import tech.overturn.model.Ledger;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

public class LedgerActivity extends AppCompatActivity {

    Long account_id;
    ListView lview;
    LocalReceiver recv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        Log.d("fcrow", "---------------- onCreate");

        TextView blink = (TextView)findViewById(R.id.backLink);
        blink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToMain();
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
        Log.d("fcrow", "-------------- refresh called");
        List<Ledger> ldata = Orm.recentLedgers(Global.getReadDb(getApplicationContext()),
                1000 * 60 * 30L);
        Log.d("fcrow", "-------------- after refresh");

        LedgerAdapter adapter = (LedgerAdapter)lview.getAdapter();

        if(adapter == null) {
            lview.setAdapter(new LedgerAdapter(this, R.layout.ledger_row, ldata));
        } else {
            adapter.clear();
            adapter.addAll(ldata);
            adapter.notifyDataSetChanged();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
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
