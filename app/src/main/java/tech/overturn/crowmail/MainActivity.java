package tech.overturn.crowmail;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import tech.overturn.crowmail.models.Account;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("fcrow","--------------------------------- hi -----------------------");
        DBHelper dbh = new DBHelper(getBaseContext());
        Log.d("fcrow","--------------------------------- after hi -----------------------");
        Account a = new Account();
        a.imapHost.value = "http://example.com";
        Log.d("fcrow","----------- before _id:"+a._id+" -----------------------");
        SQLiteDatabase db = dbh.getWritableDatabase();
        Orm.insert(db, a);
        Log.d("fcrow","----------- after _id:"+a._id+" -----------------------");
    }
}
