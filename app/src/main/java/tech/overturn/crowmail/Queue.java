package tech.overturn.crowmail;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.models.Account;
import tech.overturn.crowmail.models.CrowMessage;
import tech.overturn.crowmail.models.Ledger;

public class Queue extends Service {

    DBHelper dbh;
    Map<Integer, Account> recieving;

    @Override
    public void onCreate() {
        dbh = new DBHelper(getApplicationContext());
        recieving = new HashMap<Integer, Account>();
        Log.d("fcrow", "---------- service created");

        Ledger.fromStrings(getApplicationContext(),
                dbh.getWritableDatabase(),
                Ledger.INFO_TYPE,
                null, 
                "service created",
                "", 
                true);

        NetworkListen.listen(getApplicationContext());
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
            final Account a = Account.byId(dbh.getReadableDatabase(), account_id.intValue());

            Long message_id;
            CrowMessage msg;
            if(intent.hasExtra("message_id")) {
                message_id = intent.getLongExtra("message_id", 0);
                msg = CrowMessage.byId(dbh.getReadableDatabase(), message_id.intValue());
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
                dbh.getWritableDatabase(),
                Ledger.INFO_TYPE,
                null,
                "service destroyed",
                "",
                true);
        stopSelf();
    }

}
