package tech.overturn.crowmail;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.AccountData;

public class AccountActivity extends AppCompatActivity {

    public Account a;
    DBHelper dbh;

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
        Button btn = (Button)findViewById(R.id.accountSave);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });
    }

    public void setUpUI() {
        a.setUI("imapHost", (View) findViewById(R.id.imapHost));
        a.setUI("imapPort", (View) findViewById(R.id.imapPort));
        a.setUI("imapSslType", (View) findViewById(R.id.imapSslType));
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
}
