package tech.overturn.service;

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

import tech.overturn.model.Account;
import tech.overturn.model.CrowMessage;
import tech.overturn.model.Ledger;
import tech.overturn.util.Global;

public class Queue extends Service {

    @Override
    public void onCreate() {

        NetworkRequest req = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        new Ledger(
                null,
                null,
                new Date(),
                Ledger.INFO_TYPE,
                "service created",
                null,
                null)
        .log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
        runLoops(true);
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
            if (intent.getAction().equals(Global.TRIGGER_FETCH)) {
                Long id = intent.getLongExtra("account_id", -1L);
                new Ledger(id, Account.tableName, new Date(), Ledger.ACCOUNT_RUNNING_STATUS,
                        Ledger.RUNNING, null, null
                ).log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
                runLoops(false);
            }
            if (intent.getAction().equals(Global.TRIGGER_STOP)) {
                Long id = intent.getLongExtra("account_id", -1L);
                new Ledger(id, Account.tableName, new Date(), Ledger.ACCOUNT_RUNNING_STATUS,
                        Ledger.STOPING, null, null
                ).log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
            }
        };

        return Service.START_STICKY;
    }

    private void runLoops(Boolean initial) {
        List<Long> ids = Account.allIds(getApplicationContext());
        for(Long id: ids) {
            if (initial || Account.runStateForId(getApplicationContext(), id).equals(Ledger.STOPED)) {
                Account account = Account.byId(Global.getReadDb(getApplicationContext()), id);
                new Fetcher(getApplicationContext(), account).loop();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("fcrow", "---------- service destroyed");
        new Ledger(
                null,
                null,
                new Date(),
                Ledger.INFO_TYPE,
                "service destroyed",
                null,
                null)
        .log(Global.getWriteDb(getApplicationContext()), getApplicationContext());
        stopSelf();
    }
}
