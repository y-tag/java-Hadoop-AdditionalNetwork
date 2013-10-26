package myorg.io;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;

public class FeatureVector implements Writable {
    private int dim;
    private int[] indices;
    private float[] values;
    private float squaredNorm;
    private float label;
    private String name;

    public FeatureVector() {
        clear();
    }

    public void clear() {
        dim = 0;
        indices = new int[0];
        values = new float[0];
        squaredNorm = 0.0f;
        label = 0.0f;
        name = "";
    }

    public void set(Map<Integer, Float> map) {
        List<Integer> idxList = new ArrayList<Integer>();
        
        for (Integer i : map.keySet()) {
            if (i >= 0) {
                idxList.add(i);
            }
        }
        Collections.sort(idxList);

        int size = idxList.size();
        if (size > indices.length) {
            indices = new int[size];
            values = new float[size];
        }
        dim = size;
        squaredNorm = 0.0f;

        for (int i = 0; i < size; i++) {
            indices[i] = idxList.get(i);
            values[i] = map.get(idxList.get(i));
            squaredNorm += values[i] * values[i];
        }
    }

    public void setLabel(float label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNonZeroNum() {
        return dim;
    }

    public int getIndexAt(int i) {
        return indices[i];
    }
    
    public float getValueAt(int i) {
        return values[i];
    }

    public float getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public float getSquaredNorm() {
        return squaredNorm;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(dim);
        for (int i = 0; i < dim; i++) {
            out.writeInt(indices[i]);
            out.writeFloat(values[i]);
        }
        out.writeFloat(squaredNorm);
        out.writeFloat(label);
        Text.writeString(out, name);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();
        if (size > indices.length) {
            indices = new int[size];
            values = new float[size];
        }
        dim = size;
        for (int i = 0; i < dim; i++) {
            indices[i] = in.readInt();
            values[i] = in.readFloat();
        }
        squaredNorm = in.readFloat();
        label = in.readFloat();
        name = Text.readString(in);
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(Float.toString(label));

        for (int i = 0; i < dim; i++) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(indices[i]);
            sb.append(':');
            sb.append(values[i]);
        }

        if (! name.equals("")) {
            sb.append(" # " + name);
        }

        return sb.toString();
    }

}
