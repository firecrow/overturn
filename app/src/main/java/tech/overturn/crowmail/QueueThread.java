package tech.overturn.crowmail;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class QueueThread extends Thread {
    Handler handler;
    public QueueThread() {
        Handler handler = new Handler();
    }

    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public void run() {}
}
