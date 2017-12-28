package tech.overturn.crowmail;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import tech.overturn.crowmail.models.Account;

public class AccountActivity extends AppCompatActivity {

    public Account a;
    DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        dbh = new DBHelper(getBaseContext());
        setUpUI();
        Button btn = (Button)findViewById(R.id.accountSave);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });
    }

    public void setUpUI() {
        a = new Account();
        a.setUI("imapHost", (View) findViewById(R.id.imapHost));
        a.setUI("imapPort", (View) findViewById(R.id.imapPort));
        a.setUI("imapSslType", (View) findViewById(R.id.imapSslType));
    }

    public void save() {
        SQLiteDatabase db = dbh.getWritableDatabase();
        Orm.backfillFromUI(a.data, a.ui);
        if(a.data._id != null) {
            Orm.update(db, Account.tableName, a.data);
        } else {
            Orm.insert(db, Account.tableName, a.data);
        }
    }
}
