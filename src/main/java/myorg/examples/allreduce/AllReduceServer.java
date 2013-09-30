package myorg.examples.allreduce;

import myorg.allreduce.AllReduceCoordinator;

public class AllReduceServer {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: port");
            return;
        }
        int port = Integer.parseInt(args[0]);

        AllReduceCoordinator coordinator = new AllReduceCoordinator(port);
        coordinator.run();
    }
}


