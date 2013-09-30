package myorg.io;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.apache.hadoop.io.Writable;

import myorg.io.FeatureVector;

public class WeightVector implements Writable {
    protected float[] weightArray = null;
    protected float scaleFactor = 1.0f;
    protected float squaredNorm = 0.0f;

    public WeightVector(int dim) {
        this.weightArray = new float[dim];
        this.scaleFactor = 1.0f;
        this.squaredNorm = 0.0f;
    }

    public float innerProduct(FeatureVector x) {
        return innerProduct(x, 1.0f);
    }

    public float innerProduct(FeatureVector x, float xScale) {
        float ip = 0.0f;
        for (int i = 0; i < x.getNonZeroNum(); i++) {
            int idx = x.getIndexAt(i);
            float val = x.getValueAt(i);

            if (idx >= weightArray.length) {
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

            if (idx >= weightArray.length) {
                System.err.println("dimention over in addVector: " + idx + " >= " + weightArray.length);
                continue;
            }

            squaredNorm -= weightArray[idx] * weightArray[idx];
            weightArray[idx] += val * s;
            squaredNorm += weightArray[idx] * weightArray[idx];
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
        for (int i = 0; i < weightArray.length; i++) {
            weightArray[i] *= scaleFactor;
            squaredNorm += weightArray[i] * weightArray[i];
        }
        scaleFactor = 1.0f;
    }

    public float getSquaredNorm() {
        return squaredNorm * scaleFactor * scaleFactor;
    }

    public float getDimensions() {
        return weightArray.length;
    }

    public float getValue(int index) {
        if (index >= weightArray.length) {
            return 0.0f;
        }
        return weightArray[index];
    }

    public void setValue(int index, float value) {
        if (index < weightArray.length) {
            return;
        }
        weightArray[index] = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        rescale();
        out.writeInt(weightArray.length);
        for (int i = 0; i < weightArray.length; i++) {
            out.writeFloat(weightArray[i]);
        }
        out.writeFloat(scaleFactor);
        out.writeFloat(squaredNorm);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int dim = in.readInt();
        weightArray = new float[dim];
        for (int i = 0; i < weightArray.length; i++) {
            weightArray[i] = in.readFloat();
        }
        scaleFactor = in.readFloat();
        squaredNorm = in.readFloat();
    }

    @Override
    public String toString() {
        rescale();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < weightArray.length; i++) {
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

        return sb.toString();
    }
}

