package net.crowmail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import javax.mail.internet.InternetAddress;

import net.crowmail.model.Account;
import net.crowmail.model.CrowMessage;
import net.crowmail.service.Queue;
import net.crowmail.util.Global;
import net.crowmail.util.Orm;

public class SendActivity extends AppCompatActivity {
    Account a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Intent intent = getIntent();
        Long id = intent.getLongExtra("account_id", 0);
        this.a = new Account();
        a = (Account) Orm.byId(Global.getReadDb(getApplicationContext()),
                Account.tableName, Account.class, id);

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
    }

    public void send() {
        CrowMessage cmsg = new CrowMessage(Global.getWriteDb(getApplicationContext()));
        try {
            cmsg.from = new InternetAddress(a.email);
            String toText = ((EditText) findViewById(R.id.sendTo)).getText().toString();
            cmsg.to = InternetAddress.parse(toText);
            cmsg.subject = ((EditText) findViewById(R.id.sendSubject)).getText().toString();
            cmsg.bodyText = ((EditText) findViewById(R.id.sendBody)).getText().toString();
            cmsg.save();

            Intent queueItem = new Intent(getApplicationContext(), Queue.class);
            queueItem.setAction(Global.TRIGGER_SEND);
            queueItem.putExtra("account_id", new Long(a._id).longValue());
            queueItem.putExtra("message_id", new Long(cmsg._id).longValue());
            startService(queueItem);
        } catch(Exception e) {
            // TODO: HANDLE THESE THINGS
            Log.d("fcrow","------ error in sendActivity.send ---"+e.getMessage(), e);
            return;
        }
    }

    public void goToAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("account_id", new Long(a._id).longValue());
        startActivity(intent);
    }
}
