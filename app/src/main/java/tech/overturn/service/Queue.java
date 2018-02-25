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
import tech.overturn.model.Ledger;
import tech.overturn.util.Global;
import tech.overturn.util.Orm;

public class Queue extends Service {

    @Override
    public void onCreate() {

        NetworkRequest req = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        Orm.set(getApplicationContext(),
                Global.GLOBAL, 0L, Ledger.INFO_TYPE, new Date(), null, "service created");
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

                Orm.set(getApplicationContext(),
                        Account.tableName, id, Ledger.ACCOUNT_RUNNING_STATUS, new Date(),
                        null, Ledger.QUEUED);
                runLoops(false);
            }
            if (intent.getAction().equals(Global.TRIGGER_STOP)) {
                Long id = intent.getLongExtra("account_id", -1L);
                String existing =  Account.runStateForId(getApplicationContext(), id);
                String status = existing.equals(Ledger.RUNNING) ? Ledger.STOPING : Ledger.STOPED;
                Orm.set(getApplicationContext(),
                        Account.tableName, id, Ledger.ACCOUNT_RUNNING_STATUS, new Date(),
                        null, status);
            }
        };

        return Service.START_STICKY;
    }

    private void runLoops(Boolean initial) {
        List<Long> ids = Account.allIds(getApplicationContext());
        for(Long id: ids) {
            if (initial || Account.runStateForId(getApplicationContext(), id).equals(Ledger.QUEUED)) {
                Account account = Account.byId(getApplicationContext(), id);
                Log.d("fcrow", String.format("------------- account id:%d, is null:%b", id, account == null));
                Orm.set(getApplicationContext(),
                        Account.tableName, id, Ledger.ACCOUNT_RUNNING_STATUS, new Date(), null, Ledger.RUNNING);
                new Fetcher(getApplicationContext(), account).loop();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Orm.insert(getApplicationContext(), 
            Global.GLOBAL, 0L, Ledger.INFO_TYPE, new Date(), null, "service destroyed");
        stopSelf();
    }
}
