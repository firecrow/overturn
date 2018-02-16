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

import tech.overturn.crowmail.model.Account;
import tech.overturn.crowmail.model.AccountData;
import tech.overturn.crowmail.model.CrowMessage;
import tech.overturn.crowmail.service.Queue;
import tech.overturn.crowmail.service.Receiver;
import tech.overturn.crowmail.util.Global;
import tech.overturn.crowmail.util.Orm;

public class SendActivity extends AppCompatActivity {
    Account a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Intent intent = getIntent();
        Long id = intent.getLongExtra("account_id", 0);
        this.a = new Account();
        a.data = (AccountData) Orm.byId(Global.getReadDb(getApplicationContext()), Account.tableName, AccountData.class, id.intValue());
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

        IntentFilter filter = new IntentFilter(Global.SEND_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new Receiver(), filter);
    }

    public void send() {
        Log.d("fcrow", "---- in send in sendActivity");
        CrowMessage cmsg = new CrowMessage(Global.getWriteDb(getApplicationContext()));
        try {
            cmsg.from = new InternetAddress(a.data.email);
            String toText = ((EditText) findViewById(R.id.sendTo)).getText().toString();
            cmsg.to = InternetAddress.parse(toText);
            cmsg.data.subject = ((EditText) findViewById(R.id.sendSubject)).getText().toString();
            cmsg.data.bodyText = ((EditText) findViewById(R.id.sendBody)).getText().toString();
            cmsg.save();

            Intent queueItem = new Intent(getApplicationContext(), Queue.class);
            queueItem.setAction(Global.TRIGGER_SEND);
            queueItem.putExtra("account_id", new Long(a.data._id).longValue());
            queueItem.putExtra("message_id", new Long(cmsg.data._id).longValue());
            Log.e("fcrow", "---------------------- about to send intent");
            startService(queueItem);
        } catch(Exception e) {
            // TODO: HANDLE THESE THINGS
            Log.d("fcrow","------ error in sendActivity.send ---"+e.getMessage(), e);
            return;
        }
    }

    public void goToAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("account_id", new Long(a.data._id).longValue());
        startActivity(intent);
    }
}
