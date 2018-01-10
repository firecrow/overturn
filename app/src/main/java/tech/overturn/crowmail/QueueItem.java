package tech.overturn.crowmail.struct;

public abstract class QueueItem {
    public abstract String getAction();
    public abstract Runnable getTask();
    public abstract Long getDelay();
    public abstract Long askRetry();
}
