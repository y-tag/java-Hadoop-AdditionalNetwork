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

public class CommunicationMapper extends Mapper<Object, Text, Text, Text> {

    public static String SERVER_NAME_CONFNAME = "myorg.examples.hadoop.CommunicationMapper.serverName";
    public static String SERVER_PORT_CONFNAME = "myorg.examples.hadoop.CommunicationMapper.serverPort";

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    private String serverHostName;
    private int serverHostPort;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {

        String serverName = context.getConfiguration().get(SERVER_NAME_CONFNAME, "");
        int serverPort = context.getConfiguration().getInt(SERVER_PORT_CONFNAME, -1);
        
        if (serverName.equals("") || serverPort == -1) {
            throw new RuntimeException("server name is not defined");
        } else if (serverPort == -1) {
            throw new RuntimeException("server port is not defined");
        } else if (serverPort < 1024 || 65535 < serverPort) {
            throw new RuntimeException("server port is not valid: " + Integer.toString(serverPort));
        }

        socket = new Socket();
        int timeout = 5 * 1000; // miliseconds

        try {
            socket.connect(new InetSocketAddress(serverName, serverPort), timeout);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        inStream = new DataInputStream(
                        new BufferedInputStream(
                        socket.getInputStream()));
        outStream = new DataOutputStream(
                        new BufferedOutputStream(
                        this.socket.getOutputStream()));

        serverHostName = socket.getInetAddress().getHostName();
        serverHostPort = socket.getPort();
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        IntWritable i = new IntWritable();
        Random random = new Random();
        String serverHostNameAndPort = serverHostName + ":" + Integer.toString(serverHostPort);

        int numSend = random.nextInt(5) % 5 + 1;

        int id = context.getTaskAttemptID().getTaskID().getId();

        for (int n = 1; n <= numSend; n++) {
            i.set(id);

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
}

