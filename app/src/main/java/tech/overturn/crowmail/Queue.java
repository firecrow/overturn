package tech.overturn.crowmail;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.CrowMessage;

public class Queue extends Service {
    public static final String TRIGGER_SEND = "tech.overturn.crowmail.TRIGGER_SEND";
    public static final String SEND_ACTION = "tech.overturn.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.crowmail.SEND_STATUS";
    public static final String TRIGGER_FETCH = "tech.overturn.crowmail.TRIGGER_FETCH";
    public static final String COMPLETE = "complete";

    DBHelper dbh;
    Map<Integer, Account> recieving;

    @Override
    public void onCreate() {
        dbh = new DBHelper(getBaseContext());
        recieving = new HashMap<Integer, Account>();
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final Service self = this;
        if (intent != null) {
            Log.d("fcrow", String.format("--------------- ACTION: %s", intent.getAction()));
            if (intent.getAction().equals(TRIGGER_SEND)) {
                sendEmail(intent);
            } else if (intent.getAction().equals(TRIGGER_FETCH)) {
                startRecieving(intent);
            }
        };

        return Service.START_STICKY;
    }

    public void sendEmail(final Intent intent) {
        final Service self = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Long account_id = intent.getLongExtra("account_id", 0);
                Long message_id = intent.getLongExtra("message_id", 0);
                CrowMessage msg = CrowMessage.byId(dbh.getReadableDatabase(), message_id.intValue());
                Log.d("fcrow", String.format("--------------- in TRIGGER : from:%s to:%s", msg.from.getAddress(), msg.to[0].getAddress()));
                Account a = Account.byId(dbh.getReadableDatabase(), account_id.intValue());
                Mailer m = new Mailer(a);
                m.send(msg);
                Intent local = new Intent(SEND_ACTION);
                local.putExtra(SEND_STATUS, COMPLETE);
                local.putExtra("message_id", message_id);
                LocalBroadcastManager.getInstance(self).sendBroadcast(local);
            }
        });
        t.start();
    }

    public void startRecieving(Intent intent) {
        Long account_id = intent.getLongExtra("account_id", 0);
        final Account a = Account.byId(dbh.getReadableDatabase(), account_id.intValue());
        if(recieving.get(account_id.intValue()) != null) {
            Log.d("fcrow", String.format("--------------- already recieving"));
        } else {
            recieving.put(account_id.intValue(), a);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    new Fetcher(getBaseContext(), a).loop();
                }
            });
            t.start();
        }
    }
}
