package tech.overturn.crowmail;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tech.overturn.crowmail.models.Ledger;

public class NetworkListen extends BroadcastReceiver {
    public static boolean initialized = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        boolean up = false;
        if(info != null && info.isConnected()){
            up = true;
        }
        new Ledger(
                null,
                new Date(),
                Ledger.NETWORK_STATUS_TYPE,
                String.format("network %s", up ? "up" : "down"),
                null,
                null
        ).log(Global.getWriteDb(context), context);
        Global.setNetworkUp(context, up);
    }
}
