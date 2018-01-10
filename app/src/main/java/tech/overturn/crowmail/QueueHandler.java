package tech.overturn.crowmail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tech.overturn.crowmail.struct.QueueItem;

public class QueueHandler extends Handler {
    List<QueueItem> queue;

    public Integer enqueue(QueueItem item) throws InterruptedException {
        this.post(genRunnable(item));
    }

    public Runnable genRunnable(final QueueItem item) throws InterruptedException{
        final Handler self = this;
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Long next = item.getNext();
                    try {
                        item.getTask().run();
                    } catch (RetryException e) {
                        next = item.askRetry();
                    }
                    if(next != null && next != -1) {
                        if(next == 0) {
                            self.post(genRunnable(item));
                        }else{
                            self.postDelayed(genRunnable(item), next);
                        }
                    }
                } catch (InterruptedException e) {
                    Log.d("fcrow", "---- error creating outer runnable");
                }
            }
        };
    }
}
