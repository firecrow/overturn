package tech.overturn.crowmail;

public class QItemReqNetworkUp extends QItemReq
{
    public Boolean run()
    {
        return Global.networkUp;
    }

    public String getTrigger() {
        return Global.NETWORK_STATUS;
    }
}
