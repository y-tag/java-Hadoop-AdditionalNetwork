package myorg.allreduce;

import java.io.IOException;
import java.io.EOFException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;

public class AllReduceCoordinator<T extends Writable> implements Runnable {
    private ServerSocket serverSocket;
    private HashMap<String, LinkedList<NodeInfo>> nodeInfoQueueMap;

    public enum Command {
        registerListingInfo, requestParentInfo, connectionClosed
    }

    public class NodeInfo {
        String nodeHostName;
        int nodeHostPort;

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

    public class Worker implements Runnable {
        private Socket socket;
        private DataInputStream inStream;
        private DataOutputStream outStream;
        
        public Worker(Socket socket) throws IOException {
            this.socket = socket;
            this.inStream = new DataInputStream(
                                new BufferedInputStream(
                                this.socket.getInputStream()));
            this.outStream = new DataOutputStream(
                                new BufferedOutputStream(
                                this.socket.getOutputStream()));
        }

        @Override
        public void run() {
            boolean waiting = true;
            while (waiting) {
                try {
                    // recieve command and groupName
                    Command command = WritableUtils.readEnum(inStream, Command.class);
                    String groupName = Text.readString(inStream);

                    // get Queue for groupName
                    if (! nodeInfoQueueMap.containsKey(groupName)) {
                        nodeInfoQueueMap.put(groupName, new LinkedList<NodeInfo>());
                    }
                    Queue<NodeInfo> nodeInfoQueue = nodeInfoQueueMap.get(groupName);

                    switch (command) {
                        case registerListingInfo:
                            // receive listening info from node
                            String hostName = this.socket.getInetAddress().getHostName();
                            IntWritable hostPortWritable = new IntWritable();
                            hostPortWritable.readFields(inStream);
                            int hostPort = hostPortWritable.get();

                            // register node info
                            if (1024 <= hostPort && hostPort <= 65535) {
                                NodeInfo nodeInfo = new NodeInfo(hostName, hostPort);
                                nodeInfoQueue.offer(nodeInfo);
                            }

                            System.err.println(hostName + ":" + Integer.toString(hostPort));
                            break;

                        case requestParentInfo:
                            String parentHostName = "";
                            int parentHostPort = -1;

                            NodeInfo parentNodeInfo = nodeInfoQueue.poll();
                            if (parentNodeInfo != null) {
                                parentHostName = parentNodeInfo.getHostName();
                                parentHostPort = parentNodeInfo.getHostPort();
                            }

                            Text.writeString(outStream, parentHostName);
                            (new IntWritable(parentHostPort)).write(outStream);
                            outStream.flush();

                            break;

                        case connectionClosed:
                            waiting = false;
                            break;

                        default:
                            System.err.println("Unknown command");
                            break;
                    }

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public AllReduceCoordinator(int listenPort) throws IOException {
        this.serverSocket = new ServerSocket(listenPort);
        this.nodeInfoQueueMap = new HashMap<String, LinkedList<NodeInfo>>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                Worker worker = new Worker(socket);
                worker.run();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                break;
            }
        }
    }
}

