package tech.overturn.crowmail;

import android.app.IntentService;
import android.content.Intent;

public class Queue extends IntentService {

    public Queue(String name){
       super(name);
    }

    @Override
    public void onHandleIntent(Intent work) {
        ;
    }
}
