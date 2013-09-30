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
import java.util.List;
import java.util.ArrayList;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class AllReduceContext {
    protected ConnectionInfo coordinatorInfo;
    protected ConnectionInfo parentInfo;
    protected List<ConnectionInfo> childrenInfo;

    protected class ConnectionInfo {
        private Socket socket;
        private DataInputStream inStream;
        private DataOutputStream outStream;

        public ConnectionInfo(Socket socket) throws IOException {
            this.socket = socket;
            this.inStream = new DataInputStream(
                                new BufferedInputStream(
                                this.socket.getInputStream()));
            this.outStream = new DataOutputStream(
                                new BufferedOutputStream(
                                this.socket.getOutputStream()));
        }

        public void close() throws IOException {
            socket.close();
            System.err.println("close");
        }
        
        public DataInputStream getDataInputStream() {
            return inStream;
        }

        public DataOutputStream getDataOutputStream() {
            return outStream;
        }

        public String getHostName() {
            return socket.getInetAddress().getHostName();
        }
        
        public int getHostPort() {
            return socket.getPort();
        }
    }

    protected class ConnectionListener implements Runnable {
        private ServerSocket serverSocket;

        public ConnectionListener() throws IOException {
            this.serverSocket = new ServerSocket(0); // with unused port
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        public void run() {
            // listen to connection from child node
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ConnectionInfo info = new ConnectionInfo(socket);
                    System.err.println("child node: " + info.getHostName() + ":" + info.getHostPort() + " -> " + socket.getLocalPort());
                    childrenInfo.add(info);
                } catch (IOException e) {
                    continue;
                }
            }
        }
    }

    public AllReduceContext(
            String coordinatorHostName, int coordinatorHostPort, String groupName) throws IOException {
        int connectionTimeout = 5 * 1000; // miliseconds

        // create ServerSocket and listen to connection from children reducers
        this.childrenInfo = new ArrayList<ConnectionInfo>();
        int listenPort = -1;
        {
            ConnectionListener listener = new ConnectionListener();
            listenPort = listener.getPort();
            Thread thread = new Thread(listener);
            thread.setDaemon(true); // use daemon thread
            thread.start();
        }

        System.err.println("listen to port: " + Integer.toString(listenPort));

        // create Socket to Coordinator
        {
            Socket socket = new Socket();
            socket.connect(
                    new InetSocketAddress(coordinatorHostName, coordinatorHostPort),
                    connectionTimeout);
            this.coordinatorInfo = new ConnectionInfo(socket);
        }

        // send listening info to Coordinator
        {
            Text.writeString(coordinatorInfo.getDataOutputStream(), groupName);
            (new IntWritable(listenPort)).write(coordinatorInfo.getDataOutputStream());
            coordinatorInfo.getDataOutputStream().flush();
        }

        // receive info of parent node from Coordinator
        String parentHostName = "";
        int parentHostPort = -1;
        {
            parentHostName = Text.readString(coordinatorInfo.getDataInputStream());

            IntWritable parentHostPortWritable = new IntWritable();
            parentHostPortWritable.readFields(coordinatorInfo.getDataInputStream());
            parentHostPort = parentHostPortWritable.get();
        }

        // create Socket to parent node
        if (parentHostPort > 0) {
            Socket socket = new Socket();
            socket.connect(
                    new InetSocketAddress(parentHostName, parentHostPort),
                    connectionTimeout);
            this.parentInfo = new ConnectionInfo(socket);
            System.err.println("parent node: " + this.parentInfo.getHostName() + ":" + this.parentInfo.getHostPort());
        } else {
            this.parentInfo = null;
            System.err.println("no parent node, that is root node");
        }
    }

    public void close() throws IOException {
        coordinatorInfo.close();
        if (parentInfo != null) {
            parentInfo.close();
        }
        for (ConnectionInfo childInfo : childrenInfo) {
            childInfo.close();
        }
    }

    public boolean isRoot() {
        return (parentInfo == null);
    }

    public DataInputStream getParentDataInputStream() {
        return parentInfo.getDataInputStream();
    }
    
    public DataOutputStream getParentDataOutputStream() {
        return parentInfo.getDataOutputStream();
    }

    public Iterable<DataInputStream> getChildrenDataInputStreams() {
        List<DataInputStream> iterable = new ArrayList<DataInputStream>();
        for (ConnectionInfo childInfo : childrenInfo) {
            iterable.add(childInfo.getDataInputStream());
        }
        return iterable;
    }
    
    public Iterable<DataOutputStream> getChildrenDataOutputStreams() {
        List<DataOutputStream> iterable = new ArrayList<DataOutputStream>();
        for (ConnectionInfo childInfo : childrenInfo) {
            iterable.add(childInfo.getDataOutputStream());
        }
        return iterable;
    }

}


