package myorg.examples.allreduce;

import java.io.IOException;
import java.io.DataInputStream;

import org.apache.hadoop.io.IntWritable;

import myorg.allreduce.AllReducer;
import myorg.allreduce.AllReduceContext;

public class IntWritableSumAllReducer extends AllReducer<IntWritable> {

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
}
