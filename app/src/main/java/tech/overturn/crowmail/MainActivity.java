package tech.overturn.crowmail;

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
    }
}
