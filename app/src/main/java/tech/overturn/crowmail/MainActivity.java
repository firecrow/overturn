package tech.overturn.crowmail;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tech.overturn.crowmail.adapters.AccountAdapter;
import tech.overturn.crowmail.model.Data;
import tech.overturn.crowmail.model.Account;
import tech.overturn.crowmail.model.AccountData;
import tech.overturn.crowmail.service.Queue;
import tech.overturn.crowmail.util.Global;
import tech.overturn.crowmail.util.Orm;

public class MainActivity extends AppCompatActivity {

    ListView lview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lview = (ListView) findViewById(R.id.accountList);
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("fcrow", String.format("-------------id %d", id));
                goToAccount(id);
            }
        });

        Button btn = (Button) findViewById(R.id.addAccount);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToAccount(0);
            }
        });

        Intent serviceItem = new Intent(getApplicationContext(), Queue.class);
        serviceItem.setAction(Global.START_SERVICE);
        Log.e("fcrow", "---------------------- about to service intent");
        startService(serviceItem);

    }

    @Override
    public void onResume(){
        super.onResume();
        List<AccountData> addata = (List<AccountData>) Orm.byQuery(Global.getReadDb(getApplicationContext()),
                Account.tableName, AccountData.class, null, null, null, null);

        lview = (ListView) findViewById(R.id.accountList);
        AccountAdapter adapter = (AccountAdapter)lview.getAdapter();

        if(adapter == null) {
            lview.setAdapter(new AccountAdapter(this, R.layout.account_row, addata));
        } else {
            adapter.clear();
            adapter.addAll(addata);
            adapter.notifyDataSetChanged();
        }
    }

    public void goToAccount(long id) {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("account_id", id);
        startActivity(intent);
    }
}
