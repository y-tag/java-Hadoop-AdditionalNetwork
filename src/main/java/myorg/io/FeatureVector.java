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
    protected int[] indices;
    protected float[] values;
    protected float label;
    protected String name;

    public FeatureVector() {
        clear();
    }

    public void clear() {
        indices = new int[0];
        values = new float[0];
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
        indices = new int[size];
        values = new float[size];

        for (int i = 0; i < size; i++) {
            indices[i] = idxList.get(i);
            values[i] = map.get(idxList.get(i));
        }
    }

    public void setLabel(float label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNonZeroNum() {
        return indices.length;
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

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(indices.length);
        for (int i = 0; i < indices.length; i++) {
            out.writeInt(indices[i]);
            out.writeFloat(values[i]);
        }
        out.writeFloat(label);
        Text.writeString(out, name);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int dim = in.readInt();
        indices = new int[dim];
        values = new float[dim];
        for (int i = 0; i < dim; i++) {
            indices[i] = in.readInt();
            values[i] = in.readFloat();
        }
        label = in.readFloat();
        name = Text.readString(in);
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indices.length; i++) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(indices[i]);
            sb.append(':');
            sb.append(values[i]);
        }

        return sb.toString();
    }

}
