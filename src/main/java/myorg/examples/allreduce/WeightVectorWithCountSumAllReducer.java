package myorg.examples.allreduce;

import java.io.IOException;
import java.io.DataInputStream;

import myorg.io.WeightVector;
import myorg.io.WeightVectorWithCount;
import myorg.allreduce.AllReducer;
import myorg.allreduce.AllReduceContext;

public class WeightVectorWithCountSumAllReducer extends AllReducer<WeightVectorWithCount> {

    @Override
    protected void reduce(AllReduceContext context, WeightVectorWithCount writable) throws IOException {
        for (DataInputStream childIn : context.getChildrenDataInputStreams()) {
            WeightVectorWithCount w = new WeightVectorWithCount();
            w.readFields(childIn);
            writable.add(w);
        }

        if (! context.isRoot()) {
            writable.write(context.getParentDataOutputStream());
            context.getParentDataOutputStream().flush();
        }
    }
}
