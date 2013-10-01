package myorg.network;

public class NodeInfo {
    protected String nodeHostName;
    protected int nodeHostPort;

    public NodeInfo(String hostName, int hostPort) {
        this.nodeHostName = hostName;
        this.nodeHostPort = hostPort;
    }

    public String getHostName() {
        return nodeHostName;
    }

    public int getHostPort() {
        return nodeHostPort;
    }
}

