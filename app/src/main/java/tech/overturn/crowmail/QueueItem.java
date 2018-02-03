package tech.overturn.crowmail;

import java.util.List;

public interface QueueItem {
    public String getAction();
    public Runnable getTask() throws CrowmailException;
    public Long getDelay();
    public Long askRetry(CrowmailException e);
    public List<QItemReq> getReqs();
}
