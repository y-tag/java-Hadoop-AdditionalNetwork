package myorg.examples.allreduce;

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

import myorg.allreduce.AllReducer;
import myorg.allreduce.AllReduceContext;

public class AllReduceClient extends AllReducer<IntWritable> implements Runnable {
    private AllReduceContext context;

    public AllReduceClient(
            String coordinatorHostName, int coordinatorHostPort) throws IOException {
                this.context = new AllReduceContext(coordinatorHostName, coordinatorHostPort, "AllReduceClient");
    }

    public void close() throws IOException {
        context.close();
    }

    @Override
    protected void reduce(AllReduceContext context, IntWritable writable) throws IOException {
        for (DataInputStream childIn : context.getChildrenDataInputStreams()) {
            IntWritable w = new IntWritable();
            w.readFields(childIn);
            int sum = writable.get() + w.get();
            writable.set(sum);
        }

        if (! context.isRoot()) {
            writable.write(context.getParentDataOutputStream());
            context.getParentDataOutputStream().flush();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
        }

        IntWritable w = new IntWritable(1);
        for (int i = 0; i < 5; i++) {
            try {
                allreduce(context, w);
                System.out.println(w.get());
            } catch (IOException e) {
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: host_name port");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        for (int i = 0; i < 5; i++) {
            AllReduceClient ar = new AllReduceClient(host, port);

            Thread thread = new Thread(ar);
            thread.start();
        }
    }
}
