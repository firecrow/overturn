package tech.overturn.crowmail;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class NetworkListen {
    public static boolean initialized = false;

    public static void listen(final Context context) {
        if(initialized){
            return;
        }
        initialized = true;

        NetworkCallback callback = new NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                Log.d("fcrow", String.format("network up"));
                Global.setNetworkUp(context, true);
            }

            @Override
            public void onUnavailable() {
                Log.d("fcrow", String.format("network unavailable"));
                Global.setNetworkUp(context, false);
            }

            @Override
            public void onLost(Network network) {
                Log.d("fcrow", String.format("network lost"));
                Global.setNetworkUp(context, false);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities cap) {
                Log.d("fcrow",
                        String.format("network capabilities changed mobile:%b wifi:%b",
                                cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
                                cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        )
                );
            }
        };

        NetworkRequest req = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
                ;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.registerNetworkCallback(req, callback);
    }
}
