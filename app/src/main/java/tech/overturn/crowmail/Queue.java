package tech.overturn.crowmail;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class Queue extends Service {
    public static final String TRIGGER_SEND = "tech.overturn.crowmail.TRIGGER_SEND";
    public static final String SEND_ACTION = "tech.overturn.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.crowmail.SEND_STATUS";
    public static final String COMPLETE = "complete";

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (intent.getAction().equals(TRIGGER_SEND)) {
                Long message_id = intent.getLongExtra("message_id", 0);
                Log.d("fcrow", String.format("---------- IN SERVICE send email with id %d", message_id));
                Intent local = new Intent(SEND_ACTION);
                local.putExtra(SEND_STATUS, COMPLETE);
                SystemClock.sleep(5000);
                LocalBroadcastManager.getInstance(this).sendBroadcast(local);
                Log.d("fcrow", String.format("---------- IN SERVICE DONE WITH with id %d", message_id));
            }
        }
        return Service.START_STICKY;
    }
}
