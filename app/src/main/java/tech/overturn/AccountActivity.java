package tech.overturn;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tech.overturn.model.Account;
import tech.overturn.model.Ledger;
import tech.overturn.service.Queue;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

import java.util.Date;

public class AccountActivity extends AppCompatActivity {

    public Account a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Intent intent = getIntent();
        Long id = intent.getLongExtra("account_id", 0);
        if (id == 0) {
            this.a = new Account();
        } else {
            this.a = (Account) Orm.byId(Global.getWriteDb(getApplicationContext()),
                    Account.class, Account.tableName, id);
        }
        setUpUI();
        Orm.fillUI(a, a.ui);
        Button btn = (Button)findViewById(R.id.accountDone);
        TextView blink = (TextView)findViewById(R.id.backLink);
        TextView slink = (TextView)findViewById(R.id.stopLink);
        TextView flink = (TextView)findViewById(R.id.fetchLink);
        View.OnClickListener back = new View.OnClickListener() {
            public void onClick(View v) {
                goToMain();
            }
        };
        View.OnClickListener stop = new View.OnClickListener() {
            public void onClick(View v) {
                Intent stopItem = new Intent(getApplicationContext(), Queue.class);
                stopItem.putExtra("account_id", a._id);
                stopItem.setAction(Global.TRIGGER_STOP);
                startService(stopItem);
            }
        };
        View.OnClickListener fetch = new View.OnClickListener() {
            public void onClick(View v) {
                Intent fetchItem = new Intent(getApplicationContext(), Queue.class);
                fetchItem.putExtra("account_id", a._id);
                fetchItem.setAction(Global.TRIGGER_FETCH);
                startService(fetchItem);
            }
        };

        btn.setOnClickListener(back);
        blink.setOnClickListener(back);
        slink.setOnClickListener(stop);
        flink.setOnClickListener(fetch);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    }

    public void save() {
        SQLiteDatabase db = Global.getWriteDb(getApplicationContext());
        Orm.backfillFromUI(a, a.ui);
        a.save(db);
    }

    public void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
