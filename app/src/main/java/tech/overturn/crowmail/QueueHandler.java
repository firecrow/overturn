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

    public QueueHandler() {
        this.queue = new ArrayList<QueueItem>();
    }

    public Integer enqueue(QueueItem item) throws InterruptedException {
        this.queue.add(item);
        this.post(genRunnable(item));
    }

    public Runnable genRunnable(final QueueItem item) throws InterruptedException{
        final Handler self = this;
        return new Runnable() {
            @Override
            public void run() {
                item.task.run();
                if(item.next != null && item.next != 0) {
                    try {
                        self.postDelayed(genRunnable(item), item.next);
                    } catch (InterruptedException e) {
                        Log.d("fcrow", "---- error creating outer runnable");
                    }
                }
            }
        };
    }
}
