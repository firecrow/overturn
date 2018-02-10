package tech.overturn.crowmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import tech.overturn.crowmail.models.Ledger;

public class LedgerActivity extends AppCompatActivity {

    DBHelper dbh;
    Long account_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        dbh = new DBHelper(getBaseContext());
        Intent intent = getIntent();
        this.account_id = intent.getLongExtra("account_id", 0);
    }

    @Override
    public void onResume(){
        super.onResume();
        List<Ledger> ldata = (List<Ledger>) Orm.byQuery(dbh.getReadableDatabase(),
                Ledger.tableName,
                Ledger.class,
                "account_id = ?",
                new String[] {account_id.toString()},
                "date desc");
        Log.d("fcrow", String.format("in resume: %d %d", account_id, ldata.size()));

        ListView lview = (ListView) findViewById(R.id.ledgerList);
        LedgerAdapter adapter = (LedgerAdapter)lview.getAdapter();

        if(adapter == null) {
            lview.setAdapter(new LedgerAdapter(this, R.layout.ledger_row, ldata));
        } else {
            adapter.clear();
            adapter.addAll(ldata);
            adapter.notifyDataSetChanged();
        }
    }

}
