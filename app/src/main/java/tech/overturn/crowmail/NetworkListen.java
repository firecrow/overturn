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

import java.util.HashMap;
import java.util.Map;

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
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    String name = info.getTypeName();
                    interfacesUp.put(name, true);
                    Log.d("fcrow", String.format("network up %s", name));
                    NetworkListen.calcUp(context);
                } else {
                    Log.d("fcrow", String.format("network up null"));
                }
            }

            @Override
            public void onUnavailable() {
                Log.d("fcrow", String.format("network unavailable"));
                NetworkListen.calcUp(context);
            }

            @Override
            public void onLost(Network network) {
                NetworkInfo info = cm.getNetworkInfo(network);
                if (info != null) {
                    String name = info.getTypeName();
                    interfacesUp.remove(name);
                    Log.d("fcrow", String.format("network lost %s", name));
                    NetworkListen.calcUp(context);
                } else {
                    Log.d("fcrow", String.format("network lost null"));
                }
            }
        };

        cm.registerNetworkCallback(new NetworkRequest.Builder().build(), callback);
    }

    public static void calcUp(Context context) {
        Boolean up = interfacesUp.size() > 0;
        Global.setNetworkUp(context, up);
    }
}
