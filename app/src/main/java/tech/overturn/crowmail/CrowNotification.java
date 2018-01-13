package tech.overturn.crowmail;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class CrowNotification {
    static Integer _nextId = 25;
    static List<String> groups;
    String title;
    String content;
    String group;
    Integer iconId;
    boolean vibrate;

    Context context;

    public CrowNotification(Context context) {
        this.context = context;
        this.groups = new ArrayList<String>();
    }

    public void send(String title, String content, String group, Integer iconId, boolean vibrate) {
        this.title = title;
        this.content = content;
        this.group = group;
        this.iconId = iconId;
        this.vibrate = vibrate;
    };

    public Integer getGroupId(String group){
        Integer idx = groups.indexOf(group);
        if(idx == -1){
            groups.add(group);
            idx = groups.size()-1;
        }
        return idx;
    }

    public Integer getNextId() {
        return _nextId++;
    }

    public void send() {
        NotificationManagerCompat nmng = NotificationManagerCompat.from(context);
        Notification sum = new Notification.Builder(context)
                .setContentTitle(group)
                .setContentText(group)
                .setSmallIcon(iconId)
                .setGroupSummary(true)
                .setGroup(group)
                .build()
                ;
        nmng.notify(group, getGroupId(group), sum);
        Notification.Builder nb = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconId)
                .setGroupSummary(false)
                .setGroup(group)
                ;
        if (vibrate) {
            nb.setVibrate(new long[]{ 1000, 1000, 1000});
        }
        nmng.notify(group, getNextId(), nb.build());
    }
}
