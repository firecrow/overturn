package tech.overturn.crowmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import tech.overturn.crowmail.models.Ledger;

public class LedgerDetailActivity extends AppCompatActivity {

    DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_detail);
        dbh = new DBHelper(getBaseContext());
        Intent intent = getIntent();
        final Long account_id = intent.getLongExtra("account_id", 0);
        Long ledger_id = intent.getLongExtra("ledger_id", 0);
        Ledger ledger = (Ledger) Orm.byId(dbh.getReadableDatabase(),
                    Ledger.tableName, Ledger.class, ledger_id.intValue());
        TextView blink = (TextView)findViewById(R.id.lDetBackLink);
        blink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToList(account_id);
            }
        });
        ((TextView)findViewById(R.id.lDetType)).setText(ledger.type);
        ((TextView)findViewById(R.id.lDetDate)).setText(ledger.date.toString());
        ((TextView)findViewById(R.id.lDetLongval)).setText(ledger.longval.toString());
        ((TextView)findViewById(R.id.lDetTextval)).setText(ledger.textval);
        ((TextView)findViewById(R.id.lDetDescription)).setText(ledger.description);
    }

    public void goToList(Long account_id) {
        Intent intent = new Intent(this, LedgerActivity.class);
        Log.d("fcrow", String.format("going to back ledger list with account: %s", account_id));
        intent.putExtra("account_id", account_id.longValue());
        startActivity(intent);
    }
}
