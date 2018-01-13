package tech.overturn.crowmail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tech.overturn.crowmail.QueueItem;
import tech.overturn.crowmail.models.ErrorStatus;

public class QueueHandler extends Handler {
    List<QueueItem> queue;
    Context context;
    SQLiteDatabase db;

    public QueueHandler(Context context, SQLiteDatabase db, Looper looper) {
        super(looper);
        this.context = context;
        this.db = db;
    }

    public void enqueue(QueueItem item) throws InterruptedException {
        this.post(genRunnable(item));
    }

    public Runnable genRunnable(final QueueItem item) throws InterruptedException{
        final Handler self = this;
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Long next = item.getDelay();
                    try {
                        item.getTask().run();
                    } catch (CrowmailException e) {
                        next = item.askRetry(e);
                    }
                    if(next != null && next != -1) {
                        if(next == 0) {
                            self.post(genRunnable(item));
                        }else{
                            self.postDelayed(genRunnable(item), next);
                        }
                    }else{
                        ErrorStatus.fromStrings(context, db, "no retry: "+item.getAction(), "no retry", "", 0, true);
                    }
                } catch (InterruptedException e) {
                    Log.d("fcrow", "---- error creating outer runnable");
                } catch (Exception e) {
                    ErrorStatus.fromStrings(context, db, "run error "+item.getAction(), "error running or queuing item", "", 0, true);
                }
            }
        };
    }
}
