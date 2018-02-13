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
                        " +" + name + " -> " +interfacesUp.keySet().toString(),
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
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                String name;
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    name = info.getTypeName();
                } else {
                    name = "DEFAULT";
                }

                String caps = "";
                if(capabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    caps += "M";
                }
                if(capabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI)) {
                    caps += "W";
                }
                if(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    caps += "I";
                }

                new Ledger(
                        null,
                        new Date(),
                        Ledger.NETWORK_STATUS_TYPE,
                        " ^"+caps + name + " -> " +interfacesUp.keySet().toString(),
                        Long.valueOf(max),
                        null
                ).log(Global.getDBH(context).getWritableDatabase(), context);
                NetworkListen.calcUp(context);
            }


            @Override
            public void onLosing(Network network, int max) {
                String name;
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    name = info.getTypeName();
                } else {
                    name = "DEFAULT";
                }
                new Ledger(
                        null,
                        new Date(),
                        Ledger.NETWORK_STATUS_TYPE,
                        " ..." + name + " -> " +interfacesUp.keySet().toString(),
                        Long.valueOf(max),
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
                        " -" + name + " -> " +interfacesUp.keySet().toString(),
                        null,
                        null
                ).log(Global.getDBH(context).getWritableDatabase(), context);
                NetworkListen.calcUp(context);
            }
        };
        NetworkRequest req = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        cm.registerNetworkCallback(req, callback);
    }

    public static void calcUp(Context context) {
        Boolean up = interfacesUp.size() > 0;
        Global.setNetworkUp(context, up);
    }
}
