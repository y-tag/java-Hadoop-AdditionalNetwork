package myorg.examples.hadoop;

import java.io.IOException;
import java.io.EOFException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.util.Random;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CommunicationServer implements Runnable {

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    private String clientHostName;
    private int clientHostPort;

    public CommunicationServer(Socket socket) throws IOException {
        this.socket = socket;
        this.inStream = new DataInputStream(
                            new BufferedInputStream(
                            this.socket.getInputStream()));
        this.outStream = new DataOutputStream(
                            new BufferedOutputStream(
                            this.socket.getOutputStream()));

        this.clientHostName = socket.getInetAddress().getHostName();
        this.clientHostPort = socket.getPort();
    }

    public void run() {
        IntWritable i = new IntWritable();
        Random random = new Random();

        String clientHostNameAndPort = clientHostName + ":" + Integer.toString(clientHostPort);

        while (true) {
            try {
                i.readFields(inStream);
                System.err.println("receive '" + i.toString() + "' from " + clientHostNameAndPort);
            } catch (EOFException e) {
                System.err.println("EOFException occured. maybe connection is closed: " + clientHostNameAndPort);
                break;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                break;
            }

            i.set(random.nextInt());

            try {
                System.err.println("send '" + i.toString() + "' to " + clientHostNameAndPort);
                i.write(outStream);
                outStream.flush();
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
        if (args.length < 1) {
            System.err.println("Usage: port");
            return;
        }
        int port = Integer.parseInt(args[0]);

        ServerSocket svSocket = new ServerSocket(port);
        
        while (true) {
            Socket socket = svSocket.accept();

            CommunicationServer sv = new CommunicationServer(socket);
            Thread thread = new Thread(sv);
            thread.start();
        }

    }
}


