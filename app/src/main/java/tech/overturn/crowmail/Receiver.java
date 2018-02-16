package tech.overturn.crowmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import javax.mail.Message;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("fcrow", "--------------- hi from reciever");
        new Handler().post(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(context, "hi from reciever", Toast.LENGTH_LONG).show();
            }
        });
    }
}
