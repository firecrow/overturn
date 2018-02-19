package tech.overturn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import tech.overturn.adapters.AccountAdapter;
import tech.overturn.model.Account;
import tech.overturn.service.Queue;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

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
        startService(serviceItem);

    }

    @Override
    public void onResume(){
        super.onResume();
        List<Account> addata = (List<Account>) Orm.byQuery(Global.getReadDb(getApplicationContext()),
                Account.tableName, Account.class, null, null, null, null);

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
