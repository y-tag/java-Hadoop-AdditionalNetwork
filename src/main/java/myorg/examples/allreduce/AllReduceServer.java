package myorg.examples.allreduce;

import myorg.network.NodeInfoIndexServer;

public class AllReduceServer {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: port");
            return;
        }
        int port = Integer.parseInt(args[0]);

        NodeInfoIndexServer indexServer = new NodeInfoIndexServer(port);
        indexServer.run();
    }
}


