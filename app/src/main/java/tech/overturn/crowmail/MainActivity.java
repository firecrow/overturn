package tech.overturn.crowmail;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import tech.overturn.crowmail.models.Account;

public class MainActivity extends AppCompatActivity {

    EditText host;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        basicDebug();
        host = (EditText)findViewById(R.id.imapHost);
        Button btn = (Button)findViewById(R.id.accountSave);
        btn.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Log.d("fcrow", String.format("-------------------------%s", host.getText()));
           }
        });
    }
    public void basicDebug() {
        Log.d("fcrow","--------------------------------- hi -----------------------");
        DBHelper dbh = new DBHelper(getBaseContext());
        Log.d("fcrow","--------------------------------- after hi -----------------------");
        Account a = new Account();
        a.data.imapHost = "http://example.com";
        Log.d("fcrow","----------- before _id:"+a.data._id+" -----------------------");
        SQLiteDatabase db = dbh.getWritableDatabase();
        Orm.insert(db, Account.tableName, a.data);
        Log.d("fcrow","----------- after _id:"+a.data._id+" -----------------------");
        a.setUI("imapHost", (View) findViewById(R.id.imapHost));
        Orm.backfillFromUI(a.data, a.ui);
        Orm.update(db, Account.tableName, a.data);
    }
}
