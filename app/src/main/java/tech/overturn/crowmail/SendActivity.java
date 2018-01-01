package tech.overturn.crowmail;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.List;

import javax.mail.internet.InternetAddress;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.AccountData;
import tech.overturn.crowmail.models.CrowMessage;

public class SendActivity extends AppCompatActivity {
    Account a;
    DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        dbh = new DBHelper(getBaseContext());
        Intent intent = getIntent();
        Long id = intent.getLongExtra("account_id", 0);
        this.a = new Account();
        a.data = (AccountData) Orm.byId(dbh.getReadableDatabase(), Account.tableName, AccountData.class, id.intValue());
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send();
            }
        });
        Button backBtn = (Button) findViewById(R.id.backSendBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToAccount();
            }
        });

        IntentFilter filter = new IntentFilter(Queue.SEND_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new Receiver(), filter);
    }

    public void send() {
        Log.d("fcrow", "---- in send in sendActivity");
        CrowMessage cmsg = new CrowMessage(dbh.getWritableDatabase());
        try {
            cmsg.from = new InternetAddress(a.data.email);
            String toText = ((EditText) findViewById(R.id.sendTo)).getText().toString();
            cmsg.to = InternetAddress.parse(toText);
            cmsg.data.subject = ((EditText) findViewById(R.id.sendSubject)).getText().toString();
            cmsg.data.bodyText = ((EditText) findViewById(R.id.sendBody)).getText().toString();
            cmsg.save();

            Intent queueItem = new Intent(getApplicationContext(), Queue.class);
            queueItem.putExtra("message_id", new Long(cmsg.data._id).longValue());
            startService(queueItem);
        } catch(Exception e) {
            // TODO: HANDLE THESE THINGS
            Log.d("fcrow","------ error in sendActivity.send ---"+e.getMessage(), e);
            return;
        }

        Mailer m = new Mailer(a);
        m.send(cmsg);
    }

    public void goToAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("account_id", new Long(a.data._id).longValue());
        startActivity(intent);
    }
}
