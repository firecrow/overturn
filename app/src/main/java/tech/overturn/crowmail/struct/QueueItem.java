package tech.overturn.crowmail.struct;

public class QueueItem {
    public String action;
    public Runnable task;
    public Long next;
    public QueueItem(String action, Runnable task, Long next){
        this.action = action;
        this.task = task;
        this.next = next;
    }
}


