package tech.overturn.crowmail;

import android.app.Service;
import android.content.Context;
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

public class NetworkListen {
    public static boolean initialized = false;

    public static Map<String, Boolean> interfacesUp = new HashMap<String, Boolean>();

    public static void listen(final Context context) {
        if(initialized){
            return;
        }
        initialized = true;

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCallback callback = new NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                String name;
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    name = info.getTypeName();
                } else {
                    name = "DEFAULT";
                }
                interfacesUp.put(name, true);
                new Ledger(
                        null,
                        new Date(),
                        Ledger.NETWORK_STATUS_TYPE,
                        " +" + (name != null ? name : "null") + " -> " +interfacesUp.keySet().toString(),
                        null,
                        null
                ).log(Global.getDBH(context).getWritableDatabase(), context);
                NetworkListen.calcUp(context);
            }

            @Override
            public void onUnavailable() {
                interfacesUp.clear();
                new Ledger(
                        null,
                        new Date(),
                        Ledger.NETWORK_STATUS_TYPE,
                        "unavailable",
                        null,
                        null
                ).log(Global.getDBH(context).getWritableDatabase(), context);
                NetworkListen.calcUp(context);
            }

            @Override
            public void onLost(Network network) {
                String name;
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    name = info.getTypeName();
                } else {
                    name = "DEFAULT";
                }
                interfacesUp.remove(name);
                new Ledger(
                        null,
                        new Date(),
                        Ledger.NETWORK_STATUS_TYPE,
                        " -" + (name != null ? name : "null") + " -> " +interfacesUp.keySet().toString(),
                        null,
                        null
                ).log(Global.getDBH(context).getWritableDatabase(), context);
                NetworkListen.calcUp(context);
            }
        };

        cm.registerNetworkCallback(new NetworkRequest.Builder().build(), callback);
    }

    public static void calcUp(Context context) {
        Boolean up = interfacesUp.size() > 0;
        Global.setNetworkUp(context, up);
    }
}
