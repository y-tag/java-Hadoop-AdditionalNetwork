package myorg.allreduce;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.hadoop.io.Writable;

public class AllReducer<T extends Writable> {

    public void allreduce(AllReduceContext context, T writable) throws IOException {
        reduce(context, writable);
        broadcast(context, writable);
    }

    protected void reduce(AllReduceContext context, T writable) throws IOException {
        for (DataInputStream childIn : context.getChildrenDataInputStreams()) {
            writable.readFields(childIn);
        }

        if (! context.isRoot()) {
            writable.write(context.getParentDataOutputStream());
            context.getParentDataOutputStream().flush();
        }
    }

    protected void broadcast(AllReduceContext context, T writable) throws IOException {
        if (! context.isRoot()) {
            try {
                writable.readFields(context.getParentDataInputStream());
            } catch (Exception e) {
                System.err.println("exception occured");
            }
        }

        for (DataOutputStream childOut : context.getChildrenDataOutputStreams()) {
            writable.write(childOut);
            childOut.flush();
        }
    }
    

}

