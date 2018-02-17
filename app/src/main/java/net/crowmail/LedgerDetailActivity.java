package net.crowmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.crowmail.model.Ledger;
import net.crowmail.util.Global;
import net.crowmail.util.Orm;

public class LedgerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_detail);

        Intent intent = getIntent();
        final Long account_id = intent.getLongExtra("account_id", 0);
        Long ledger_id = intent.getLongExtra("ledger_id", 0);
        Ledger ledger = (Ledger) Orm.byId(Global.getReadDb(getApplicationContext()),
                    Ledger.tableName, Ledger.class, ledger_id);

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
        intent.putExtra("account_id", account_id);
        startActivity(intent);
    }
}
