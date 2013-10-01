package myorg.network;

import java.util.LinkedList;
import java.util.HashMap;

public class NodeInfoManager {
    private int defaultConnectionNumLimit = 1;
    private HashMap<String, LinkedList<InnerInfo>> innerInfoQueueMap;

    private class InnerInfo extends NodeInfo {
        public int connectionRemainNum;

        public InnerInfo(String hostName, int hostPort) {
            this(hostName, hostPort, defaultConnectionNumLimit);
        }

        public InnerInfo(String hostName, int hostPort, int connectionNumLimit) {
            super(hostName, hostPort);
            connectionRemainNum = connectionNumLimit;
        }

        public int getConnectionRemainNum() {
            return connectionRemainNum;
        }

        public void setConnectionRemainNum(int num) {
            connectionRemainNum = num;
        }
    }

    public NodeInfoManager() {
        this(1);
    }

    public NodeInfoManager(int defaultConnectionNumLimit) {
        innerInfoQueueMap = new HashMap<String, LinkedList<InnerInfo>>();
        this.defaultConnectionNumLimit = defaultConnectionNumLimit;
    }

    public synchronized NodeInfo pop(String groupName) {
        if (! innerInfoQueueMap.containsKey(groupName)) {
            return null;
        }

        InnerInfo info = innerInfoQueueMap.get(groupName).peek();

        if (info != null) {
            int remain = info.getConnectionRemainNum();
            remain--;
            if (remain <= 0) {
                innerInfoQueueMap.get(groupName).remove();
            } else {
                innerInfoQueueMap.get(groupName).peek().setConnectionRemainNum(remain);
            }
        }

        return info;
    }

    public void push(String groupName, NodeInfo info) {
        push(groupName, info, defaultConnectionNumLimit);
    }

    public synchronized void push(String groupName, NodeInfo info, int connectionNumLimit) {
        if (connectionNumLimit <= 0) {
            return;
        }

        if (! innerInfoQueueMap.containsKey(groupName)) {
            innerInfoQueueMap.put(groupName, new LinkedList<InnerInfo>());
        }

        InnerInfo innerInfo = new InnerInfo(info.getHostName(), info.getHostPort(), connectionNumLimit);

        innerInfoQueueMap.get(groupName).offer(innerInfo);
    }

}
