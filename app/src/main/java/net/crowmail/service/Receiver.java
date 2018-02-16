package net.crowmail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import javax.mail.Message;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("fcrow", String.format("--------------- hi from reciever"));
        if(intent == null) {
            Log.d("fcrow", String.format("--------------- returning"));
            return;
        }
        Log.d("fcrow", String.format("--------------- receiver action:", intent.getAction()));
    }
}
