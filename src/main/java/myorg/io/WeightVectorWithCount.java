package myorg.io;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.apache.hadoop.io.Writable;

import myorg.io.WeightVector;

public class WeightVectorWithCount implements Writable {
    protected int count;
    protected WeightVector weight;

    public WeightVectorWithCount() {
        this.count = 0;
        this.weight = new WeightVector();
    }

    public WeightVectorWithCount(int count, WeightVector weight) {
        this.count = count;
        this.weight = weight;
    }

    public int getCount() {
        return count;
    }

    public WeightVector getWeight() {
        return weight;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setWeight(WeightVector weight) {
        this.weight = weight;
    }

    public void add(WeightVectorWithCount wwc) {
        count += wwc.getCount();
        weight.addVector(wwc.getWeight());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(count);
        if (weight == null) {
            weight = new WeightVector();
        }
        weight.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        count = in.readInt();
        weight = new WeightVector();
        weight.readFields(in);
    }
}


