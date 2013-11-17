package myorg.io;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Writable;

import myorg.io.FeatureVector;

public class WeightVector implements Writable {
    private int dim = 0;
    private float[] weightArray = null;
    private float scaleFactor = 1.0f;
    private float squaredNorm = 0.0f;

    public WeightVector() {
        this(0);
    }

    public WeightVector(int dim) {
        this.dim = dim;
        this.weightArray = new float[dim];
        this.scaleFactor = 1.0f;
        this.squaredNorm = 0.0f;
    }

    public WeightVector(String inString) {
        setFromString(inString);
    }

    public float innerProduct(FeatureVector x) {
        return innerProduct(x, 1.0f);
    }

    public float innerProduct(FeatureVector x, float xScale) {
        if (xScale == 0.0f) {
            return 0.0f;
        }

        float ip = 0.0f;
        for (int i = 0; i < x.getNonZeroNum(); i++) {
            int idx = x.getIndexAt(i);
            float val = x.getValueAt(i);

            if (idx >= dim) {
                continue;
            }

            ip += weightArray[idx] * val;
        }

        return ip * scaleFactor * xScale;
    }

    public void addVector(FeatureVector x) {
        addVector(x, 1.0f);
    }

    public void addVector(FeatureVector x, float xScale) {
        float s = xScale / scaleFactor;
        for (int i = 0; i < x.getNonZeroNum(); i++) {
            int idx = x.getIndexAt(i);
            float val = x.getValueAt(i);

            if (idx >= dim) {
                System.err.println("dimention over in addVector: " + idx + " >= " + dim);
                continue;
            }

            squaredNorm -= weightArray[idx] * weightArray[idx];
            weightArray[idx] += val * s;
            squaredNorm += weightArray[idx] * weightArray[idx];
        }
    }

    public void addVector(WeightVector x) {
        addVector(x, 1.0f);
    }

    public void addVector(WeightVector x, float xScale) {
        float s = xScale / scaleFactor;

        if (x.getDimensions() > dim) {
            float[] newArray = new float[x.getDimensions()];
            for (int i = 0; i < dim; i++) {
                newArray[i] = weightArray[i];
            }
            weightArray = newArray;
            dim = x.getDimensions();
        }

        for (int i = 0; i < x.getDimensions(); i++) {
            squaredNorm -= weightArray[i] * weightArray[i];
            weightArray[i] += x.getValue(i) * s;
            squaredNorm += weightArray[i] * weightArray[i];
        }
    }

    public void addVector(float[] x) {
        addVector(x, 1.0f);
    }

    public void addVector(float[] x, float xScale) {
        float s = xScale / scaleFactor;
        for (int i = 0; i < x.length; i++) {
            if (i >= dim) {
                System.err.println("dimention over in addVector: " + i + " >= " + dim);
                break;
            }

            squaredNorm -= weightArray[i] * weightArray[i];
            weightArray[i] += x[i] * s;
            squaredNorm += weightArray[i] * weightArray[i];
        }
    }

    public void scale(float xScale) {
        scaleFactor *= xScale;
        if (Math.abs(scaleFactor) < 1e-10) {
            rescale();
        }
    }

    private void rescale() {
        squaredNorm = 0.0f;
        for (int i = 0; i < dim; i++) {
            weightArray[i] *= scaleFactor;
            squaredNorm += weightArray[i] * weightArray[i];
        }
        scaleFactor = 1.0f;
    }

    public float getSquaredNorm() {
        return squaredNorm * scaleFactor * scaleFactor;
    }

    public int getDimensions() {
        return dim;
    }

    public float getValue(int index) {
        if (index >= dim) {
            return 0.0f;
        }
        return weightArray[index] * scaleFactor;
    }

    public void setValue(int index, float value) {
        if (index < dim) {
            weightArray[index] = value / scaleFactor;
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        rescale();
        out.writeInt(dim);
        int nonzeroNum = 0;
        for (int i = 0; i < dim; i++) {
            if (weightArray[i] != 0.0f) {
                nonzeroNum++;
            }
        }
        out.writeInt(nonzeroNum);
        for (int i = 0; i < dim; i++) {
            if (weightArray[i] != 0.0f) {
                out.writeInt(i);
                out.writeFloat(weightArray[i]);
            }
        }
        out.writeFloat(scaleFactor);
        out.writeFloat(squaredNorm);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();
        if (size > weightArray.length) {
            weightArray = new float[size];
        }
        dim = size;
        int nonzeroNum = in.readInt();
        for (int i = 0; i < nonzeroNum; i++) {
            int j = in.readInt();
            weightArray[j] = in.readFloat();
        }
        scaleFactor = in.readFloat();
        squaredNorm = in.readFloat();
    }

    @Override
    public String toString() {
        rescale();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dim; i++) {
            if (weightArray[i] == 0.0f) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(i);
            sb.append(':');
            sb.append(weightArray[i]);
        }

        sb.append(" # dim:" + Integer.toString(dim));

        return sb.toString();
    }

    public void setFromString(String inString) {
        this.weightArray = new float[0];
        this.scaleFactor = 1.0f;
        this.squaredNorm = 0.0f;

        if (inString == null || "".equals(inString)) {
            return;
        }

        int idx = inString.indexOf('#'); // comment part
        String body = (idx > 0) ? inString.substring(0, idx).trim() : inString;
        String comment = (idx > 0) ? inString.substring(idx + 1).trim() : "";

        StringTokenizer st;

        st = new StringTokenizer(body, " \t\r\n:");
        if (! st.hasMoreTokens()) {
            return;
        }

        Map<Integer, Float> tmpMap = new HashMap<Integer, Float>();
        int maxFeatureId = -1;

        while (st.hasMoreTokens()) {
            String key = st.nextToken();

            if (! st.hasMoreTokens()) {
                break;
            }

            String val = st.nextToken();

            try {
                int k = Integer.parseInt(key);
                float v = Float.parseFloat(val);
                tmpMap.put(k, v);

                if (k > maxFeatureId) {
                    maxFeatureId = k;
                }
            } catch (NumberFormatException e) {
            }
        }

        dim = -1;
        if (! comment.equals("")) {
            st = new StringTokenizer(comment, " \t\r\n:");
            while (st.hasMoreTokens()) {
                String key = st.nextToken();

                if (! st.hasMoreTokens()) {
                    break;
                }

                String val = st.nextToken();

                if (key.equals("dim")) {
                    try {
                        dim = Integer.parseInt(val);
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        
        if (maxFeatureId + 1 > dim) {
            dim = maxFeatureId + 1;
        }

        this.weightArray = new float[dim];
        for (Integer key : tmpMap.keySet()) {
            weightArray[key.intValue()] = tmpMap.get(key).floatValue();
        }

    }
}


