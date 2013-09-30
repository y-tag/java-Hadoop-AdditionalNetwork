package myorg.examples.hadoop;

import java.io.IOException;
import java.io.EOFException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Random;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CommunicationClient implements Runnable {

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    private String serverHostName;
    private int serverHostPort;

    public CommunicationClient(String hostName, int port) throws IOException {
        this.socket = new Socket();
        int timeout = 5 * 1000; // miliseconds

        try {
            socket.connect(new InetSocketAddress(hostName, port), timeout);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        this.inStream = new DataInputStream(
                            new BufferedInputStream(
                            this.socket.getInputStream()));
        this.outStream = new DataOutputStream(
                            new BufferedOutputStream(
                            this.socket.getOutputStream()));

        this.serverHostName = socket.getInetAddress().getHostName();
        this.serverHostPort = socket.getPort();
    }

    public void run() {
        IntWritable i = new IntWritable();
        Random random = new Random();
        String serverHostNameAndPort = serverHostName + ":" + Integer.toString(serverHostPort);

        int numSend = random.nextInt(5) % 5 + 1;

        for (int n = 1; n <= numSend; n++) {
            i.set(random.nextInt());

            try {
                System.err.println("send '" + i.toString() + "' to " + serverHostNameAndPort);
                i.write(outStream);
                outStream.flush();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                break;
            }

            try {
                i.readFields(inStream);
                System.err.println("receive '" + i.toString() + "' from " + serverHostNameAndPort);
            } catch (EOFException e) {
                System.err.println("EOFException occured. maybe connection is closed: " + serverHostNameAndPort);
                break;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                break;
            }
        }

        try {
            outStream.close();
            inStream.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: host_name port");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        CommunicationClient cl = new CommunicationClient(host, port);
        cl.run();
    }
}
