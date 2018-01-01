package tech.overturn.crowmail;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Queue extends IntentService {

    public Queue() {
        super("CrowQueue");
    }

    @Override
    public void onHandleIntent(Intent work) {
        Long message_id = work.getLongExtra("message_id", 0);
        Log.d("fcrow", String.format("-------------------------------- IN INTENT SERVICE send email with id %d", message_id));
    }
}
