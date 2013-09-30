package myorg.examples.allreduce;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import myorg.allreduce.AllReducer;
import myorg.allreduce.AllReduceContext;

public class AllReduceMapper extends Mapper<Object, Text, Text, Text> {
    public static String SERVER_NAME_CONFNAME = "myorg.examples.allreduce.AllReduceMapper.serverName";
    public static String SERVER_PORT_CONFNAME = "myorg.examples.allreduce.AllReduceMapper.serverPort";

    private AllReduceContext allreduceContext;
    private AllReducer<IntWritable> allreducer;

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

        String jobId = context.getJobID().toString();

        this.allreduceContext = new AllReduceContext(serverName, serverPort, jobId);
        this.allreducer = new IntWritableSumAllReducer();
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        try {
            Thread.sleep(2 * 1000); 
        } catch (InterruptedException e) {
        }

        IntWritable w = new IntWritable(1);
        for (int i = 0; i < 5; i++) {
            try {
                allreducer.allreduce(allreduceContext, w);
                System.err.println(w.get());
            } catch (IOException e) {
                break;
            }
        }

        context.write(new Text(w.toString()), new Text());
    }
}

