package tech.overturn.crowmail;

public interface QueueItem {
    public String getAction();
    public Runnable getTask() throws CrowmailException;
    public Long getDelay();
    public Long askRetry(Exception e);
}
