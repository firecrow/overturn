package net.crowmail.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import net.crowmail.model.Account;
import net.crowmail.model.CrowMessage;
import net.crowmail.model.Ledger;
import net.crowmail.util.Global;

public class Queue extends Service {

    Map<Integer, Account> recieving;

    @Override
    public void onCreate() {
        recieving = new HashMap<Integer, Account>();
        Log.d("fcrow", "---------- service created");

        Ledger.fromStrings(getApplicationContext(),
                Global.getWriteDb(getApplicationContext()),
                Ledger.INFO_TYPE,
                null, 
                "service created",
                "", 
                true);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final Service self = this;

        if (intent != null && !intent.getAction().equals(Global.START_SERVICE)) {
            Long account_id = intent.getLongExtra("account_id", 0);
            final Account a = Account.byId(Global.getReadDb(getApplicationContext()), account_id.intValue());

            Long message_id;
            CrowMessage msg;
            if(intent.hasExtra("message_id")) {
                message_id = intent.getLongExtra("message_id", 0);
                msg = CrowMessage.byId(Global.getReadDb(getApplicationContext()), message_id.intValue());
            }

            Log.d("fcrow", String.format("--------------- ACTION: %s", intent.getAction()));
            if (intent.getAction().equals(Global.TRIGGER_SEND)) {
                // Mailer m = new Mailer(a);
            } else if (intent.getAction().equals(Global.TRIGGER_FETCH)) {
                if(recieving.get(account_id.intValue()) != null) {
                    Log.d("fcrow", String.format("--------------- already recieving"));
                } else {
                    new Fetcher(getApplicationContext(), a).loop();
                }
            }
        };

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("fcrow", "---------- service destroyed");
        Ledger.fromStrings(getApplicationContext(),
                Global.getWriteDb(getApplicationContext()),
                Ledger.INFO_TYPE,
                null,
                "service destroyed",
                "",
                true);
        stopSelf();
    }

}
