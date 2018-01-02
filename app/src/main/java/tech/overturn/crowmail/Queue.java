package tech.overturn.crowmail;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class Queue extends IntentService {
    public static final String SEND_ACTION = "tech.overturn.crowmail.SEND_ACTION";
    public static final String SEND_STATUS = "tech.overturn.crowmail.SEND_STATUS";
    public static final String COMPLETE = "complete";

    public Queue() {
        super("CrowQueue");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onHandleIntent(Intent work) {
        Long message_id = work.getLongExtra("message_id", 0);
        Log.d("fcrow", String.format("---------- IN SERVICE send email with id %d", message_id));
        Intent local = new Intent(SEND_ACTION);
        local.putExtra(SEND_STATUS, COMPLETE);
        SystemClock.sleep(1000);
        LocalBroadcastManager.getInstance(this).sendBroadcast(local);
    }
}
