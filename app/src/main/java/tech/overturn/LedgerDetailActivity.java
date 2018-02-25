package tech.overturn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import tech.overturn.model.Ledger;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

public class LedgerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_detail);

        Intent intent = getIntent();
        Long ledger_id = intent.getLongExtra("ledger_id", 0);
        Ledger ledger = (Ledger) Orm.getLedgerById(Global.getReadDb(getApplicationContext()), ledger_id);

        TextView blink = (TextView)findViewById(R.id.lDetBackLink);
        blink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToList();
            }
        });
        ((TextView)findViewById(R.id.lDetId)).setText(ledger._id.toString());
        ((TextView)findViewById(R.id.lDetParentId)).setText(ledger.parent_id.toString());
        ((TextView)findViewById(R.id.lDetEntity)).setText(ledger.entity);
        ((TextView)findViewById(R.id.lDetType)).setText(ledger.type);
        ((TextView)findViewById(R.id.lDetDate)).setText(ledger.date.toString());
        ((TextView)findViewById(R.id.lDetLongval)).setText(ledger.longval.toString());
        ((TextView)findViewById(R.id.lDetTextval)).setText(ledger.strval);
    }

    public void goToList() {
        Intent intent = new Intent(this, LedgerActivity.class);
        startActivity(intent);
    }
}
