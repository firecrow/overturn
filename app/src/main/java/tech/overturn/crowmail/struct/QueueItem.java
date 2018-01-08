package tech.overturn.crowmail.struct;

public class QueueItem {
    public String action;
    public Runnable task;
    public Long next;
}


