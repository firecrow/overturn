package tech.overturn.crowmail;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class Global {
    public static final String CROWMAIL = "crowmail";
    public static final String CROWMAIL_ERROR = "crowmail_error";
    public static final String TRIGGER_SEND = "tech.overturn.crowmail.TRIGGER_SEND";
    public static final String SEND_ACTION = "tech.overturn.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.crowmail.SEND_STATUS";
    public static final String TRIGGER_FETCH = "tech.overturn.crowmail.TRIGGER_FETCH";
    public static final String COMPLETE = "complete";
    public static final String GLOBAL_BROADCAST = "tech.overturn.crowmail.GLOBAL_BROADCAST";
    public static final String START_SERVICE = "tech.overturn.crowmail.START_SERVICE";
    public static final String NETWORK_STATUS = "tech.overturn.crowmail.NETWORK_STATUS";

    public static boolean networkUp = false;

    public static void setNetworkUp(Context context, boolean up) {
        Log.d("fcrow", String.format("---- network has changed:%b", up));
        if(networkUp == up){
            return;
        }
        networkUp = up;
        Intent intent = new Intent();
        intent.setAction(NETWORK_STATUS);
        Bundle b = new Bundle();
        b.putBoolean("up", up);
        intent.putExtras(b);
        context.sendBroadcast(intent);
    }
}
