package tech.overturn.crowmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.AccountData;

public class AccountActivity extends AppCompatActivity {

    public Account a;
    DBHelper dbh;
    LocalReciever localReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        dbh = new DBHelper(getBaseContext());
        Intent intent = getIntent();
        Long id = intent.getLongExtra("account_id", 0);
        if (id == 0) {
            this.a = new Account();
        } else {
            this.a = new Account();
            a.data = (AccountData) Orm.byId(dbh.getReadableDatabase(), Account.tableName, AccountData.class, id.intValue());
            Log.d("fcrow", String.format("------------_id:%d imapHost:%s", a.data._id, a.data.imapHost));
        }
        setUpUI();
        Orm.fillUI(a.data, a.ui);
        Button btn = (Button)findViewById(R.id.accountDone);
        TextView blink = (TextView)findViewById(R.id.backLink);
        TextView slink = (TextView)findViewById(R.id.sendLink);
        TextView flink = (TextView)findViewById(R.id.fetchLink);
        TextView llink = (TextView)findViewById(R.id.ledgerLink);
        View.OnClickListener back = new View.OnClickListener() {
            public void onClick(View v) {
                goToMain();
            }
        };
        View.OnClickListener send = new View.OnClickListener() {
            public void onClick(View v) {
                goToSend(a.data._id);
            }
        };
        View.OnClickListener fetch = new View.OnClickListener() {
            public void onClick(View v) {
                Intent fetchItem = new Intent(getApplicationContext(), Queue.class);
                fetchItem.setAction(Global.TRIGGER_FETCH);
                fetchItem.putExtra("account_id", new Long(a.data._id).longValue());
                Log.e("fcrow", "---------------------- about to fetch intent");
                startService(fetchItem);
            }
        };
        View.OnClickListener ledger = new View.OnClickListener() {
            public void onClick(View v) {
                goToLedger(new Long(a.data._id));
            }
        };
        btn.setOnClickListener(back);
        blink.setOnClickListener(back);
        slink.setOnClickListener(send);
        flink.setOnClickListener(fetch);
        llink.setOnClickListener(ledger);

        localReciever = new LocalReciever();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.NETWORK_STATUS);
        registerReceiver(localReciever, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localReciever);
    }

    @Override
    public void onPause(){
        super.onPause();
        save();
    }

    public void setUpUI() {
        a.setUI("email", (View) findViewById(R.id.email));
        a.setUI("name", (View) findViewById(R.id.name));
        a.setUI("user", (View) findViewById(R.id.user));
        a.setUI("password", (View) findViewById(R.id.password));
        a.setUI("imapHost", (View) findViewById(R.id.imapHost));
        a.setUI("imapPort", (View) findViewById(R.id.imapPort));
        a.setUI("imapSslType", (View) findViewById(R.id.imapSslType));
        a.setUI("smtpHost", (View) findViewById(R.id.smtpHost));
        a.setUI("smtpPort", (View) findViewById(R.id.smtpPort));
        a.setUI("smtpSslType", (View) findViewById(R.id.smtpSslType));
    }

    public void save() {
        Log.d("fcrow", String.format("-----in save: _id:%d imapHost:%s", a.data._id, a.data.imapHost));
        SQLiteDatabase db = dbh.getWritableDatabase();
        Orm.backfillFromUI(a.data, a.ui);
        Log.d("fcrow", String.format("-----in save after: _id:%d imapHost:%s", a.data._id, a.data.imapHost));
        if(a.data._id != null) {
            Orm.update(db, Account.tableName, a.data);
        } else {
            Orm.insert(db, Account.tableName, a.data);
        }
    }

    public void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }
    public void goToLedger(Long id) {
        Intent intent = new Intent(this, LedgerActivity.class);
        Log.d("fcrow",String.format("go to ledger %d", id));
        intent.putExtra("account_id", id.longValue());
        startActivity(intent);
    }
    public void goToSend(long id) {
        Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra("account_id", id);
        startActivity(intent);
    }

    private class LocalReciever extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("fcrow", String.format("----- account recieved action:", intent.getAction()));
        }
    }
}
