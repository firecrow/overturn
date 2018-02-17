package net.crowmail.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.crowmail.model.Account;
import net.crowmail.model.CrowMessage;
import net.crowmail.model.Ledger;
import net.crowmail.util.Global;

public class Queue extends Service {

    Map<Integer, Account> recieving;
    LocalNetwork localNetwork;

    @Override
    public void onCreate() {
        recieving = new HashMap<Integer, Account>();

        NetworkRequest req = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        localNetwork = new LocalNetwork(getApplicationContext());
        Global.getCm(getApplicationContext()).registerNetworkCallback(req, localNetwork);

        new Ledger(
                null,
                new Date(),
                Ledger.INFO_TYPE,
                "service created",
                null,
                null)
        .log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
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
            final Account a = Account.byId(Global.getReadDb(getApplicationContext()),
                    account_id);

            Long message_id;
            CrowMessage msg;
            if(intent.hasExtra("message_id")) {
                message_id = intent.getLongExtra("message_id", 0);
                msg = CrowMessage.byId(Global.getReadDb(getApplicationContext()),
                        message_id);
            }

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
        new Ledger(
                null,
                new Date(),
                Ledger.INFO_TYPE,
                "service destroyed",
                null,
                null)
        .log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
        Global.getCm(getApplicationContext()).unregisterNetworkCallback(localNetwork);
        stopSelf();
    }

    public class LocalNetwork extends ConnectivityManager.NetworkCallback {

        Context context;
        ConnectivityManager cm;
        Map<String, Boolean> networks;

        public LocalNetwork(Context context) {
            this.context = context;
            this.networks = new HashMap<String, Boolean>();
        }

        @Override
        public void onAvailable(Network network){
            NetworkInfo info = Global.getCm(context).getNetworkInfo(network);
            networks.put(info.getTypeName(), true);
            logConnected();
        }

        @Override
        public void onLost(Network network){
            NetworkInfo info = Global.getCm(context).getNetworkInfo(network);
            networks.remove(info.getTypeName());
            logConnected();
        }

        public void logConnected() {
            new Ledger(
                    null,
                    new Date(),
                    Ledger.NETWORK_STATUS_TYPE,
                    String.format("cb net %s", networks.keySet().toString()),
                    null,
                    null
            ).log(Global.getWriteDb(context), context);
        }
    }
}
