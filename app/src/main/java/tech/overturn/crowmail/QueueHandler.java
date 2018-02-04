package tech.overturn.crowmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.overturn.crowmail.QueueItem;
import tech.overturn.crowmail.models.Status;

public class QueueHandler extends Handler {
    List<QueueItem> queue;
    Context context;
    SQLiteDatabase db;
    private static Map<String, List<QueueItem>> reqsAwaiting;
    ReqReciever reqReciever;

    public QueueHandler(Context context, SQLiteDatabase db, Looper looper) {
        super(looper);
        this.context = context;
        this.db = db;
        this.reqsAwaiting = new HashMap<String, List<QueueItem>>();

        reqReciever = new ReqReciever(this);
    }

    public void stopReciever() {
        context.unregisterReceiver(reqReciever);
    }

    public void enqueue(QueueItem item) throws InterruptedException {
        Log.d("fcrow", "--- enqueue");
        this.post(genRunnable(item));
    }

    public Boolean reqsOrAwait(QueueItem item)
    {
        Log.d("fcrow", "----- reqs or wait");
        List<QItemReq> reqs = item.getReqs();
        Integer failures = 0;
        for (QItemReq req: reqs)
        {
            Log.d("fcrow", String.format("----- reqs or wait for:%s", req.getTrigger()));
            if(!req.run()){
                List<QueueItem> list = null;
                if (!reqsAwaiting.containsKey(req.getTrigger()))
                {
                    Log.d("fcrow", "----- reqs or wait new");
                    list = new ArrayList<QueueItem>();
                    reqsAwaiting.put(req.getTrigger(), list);
                    context.registerReceiver(reqReciever, new IntentFilter(req.getTrigger()));
                    Log.d("fcrow", "----- reqs or wait new after");
                } else {
                    Log.d("fcrow", "----- reqs or wait existing");
                    list = reqsAwaiting.get(req.getTrigger());
                }
                list.add(item);
                failures++;
            }
        }
        Log.d("fcrow", String.format("----- reqs or wait failures:%d", failures));
        return failures == 0;
    }

    public Boolean checkReqs(QueueItem item)
    {
        List<QItemReq> reqs = item.getReqs();
        for (QItemReq req: reqs)
        {
            if(!req.run()){
                return false;
            }
        }
        return true;
    }


    public Runnable genRunnable(final QueueItem item) {
        final Handler self = this;
        return new Runnable() {
            @Override
            public void run() {
                try {
                    if(!reqsOrAwait(item)){
                       return;
                    }
                    Long next = item.getDelay();
                    try {
                        item.getTask().run();
                    } catch (CrowmailException e) {
                        next = item.askRetry(e);
                    }
                    if(next != null && next != -1L) {
                        if(next == 0) {
                            self.post(genRunnable(item));
                        }else{
                            self.postDelayed(genRunnable(item), next);
                        }
                    }else{
                        Status.fromStrings(context, db, Status.ERROR_TYPE, null, "no retry: ", "", true);
                    }
                } catch (Exception e) {
                    Status.fromStrings(context, db,  Status.ERROR_TYPE, null, "run error", item.getAction()+">"+e.getClass().getName(), true);
                }
            }
        };
    }

    private class ReqReciever extends BroadcastReceiver {

        QueueHandler handler;

        public ReqReciever(QueueHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("fcrow", String.format("--- update reqs for :%s", intent.getAction()));
            List<QueueItem> items = reqsAwaiting.get(intent.getAction());
            List<QueueItem> failing = new ArrayList<QueueItem>();
            for (QueueItem item: items)
            {
                if (checkReqs(item)) {
                    try {
                        handler.enqueue(item);
                    } catch (InterruptedException e) {
                        Log.e("fcrow", "---- InterruptedException error enqueuing back into the queue");
                    }
                } else {
                    failing.add(item);
                }
            }
            reqsAwaiting.put(intent.getAction(), failing);
        }

    }
}
