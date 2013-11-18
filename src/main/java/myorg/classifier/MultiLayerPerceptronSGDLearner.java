package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightMatrix;

public class MultiLayerPerceptronSGDLearner {

    public static void learnWithStochasticLoop(
        List<FeatureVector> fVecList,
        float eta0, float lambda, int numIters,
        List<WeightMatrix> wList, List<WeightVector> bList
    ) {
        if (wList == null || bList == null || wList.size() != bList.size()) {
            return;
        }

        int dataSize = fVecList.size();

        Random rnd = new Random(0x5EED);

        for (int i = 1; i <= numIters; i++) {
            float eta = eta0 / (1.0f + eta0 * lambda * i);

            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);

            learnWithStochasticOneStep(fVec, eta, lambda, wList, bList);
        }
    }

    public static void learnWithStochasticOneStep(
        FeatureVector fVec, float eta, float lambda,
        List<WeightMatrix> wList, List<WeightVector> bList
    ) {
        int y = (int)fVec.getLabel();
        if (y < 0) { return; }

        int hNum = wList.size() - 1;
        float[][] hArrays = new float[hNum][];

        hArrays[0] = propagateForward(wList.get(0), bList.get(0), fVec);
        for (int l = 1; l < hNum; l++) {
            hArrays[l] = propagateForward(wList.get(l), bList.get(l), hArrays[l-1]);
        }
        float[] oArray = outputWithSoftmax(wList.get(hNum), bList.get(hNum), hArrays[hNum-1]);

        float[] propArray = null;

        regularize(eta, lambda, wList.get(hNum), bList.get(hNum));
        propArray = learnByOutputWithSoftmax(hArrays[hNum-1], y, oArray, eta, wList.get(hNum), bList.get(hNum));
        for (int l = hNum-1; l > 0; l--) {
            regularize(eta, lambda, wList.get(l), bList.get(l));
            propArray = propagateBackward(hArrays[l-1], hArrays[l], propArray, eta, wList.get(l), bList.get(l));
        }
        regularize(eta, lambda, wList.get(0), bList.get(0));
        propagateBackward(fVec, hArrays[0], propArray, eta, wList.get(0), bList.get(0));
    }

    public static float[] predict(
        FeatureVector fVec, List<WeightMatrix> wList, List<WeightVector> bList
    ) {
        int hNum = wList.size() - 1;
        float[][] hArrays = new float[hNum][];

        hArrays[0] = propagateForward(wList.get(0), bList.get(0), fVec);
        for (int l = 1; l < hNum; l++) {
            hArrays[l] = propagateForward(wList.get(l), bList.get(l), hArrays[l-1]);
        }
        float[] oArray = outputWithSoftmax(wList.get(hNum), bList.get(hNum), hArrays[hNum-1]);

        return oArray;
    }

    private static float[] propagateForward(WeightMatrix w, WeightVector b, FeatureVector fVec) {
        float[] retArray = w.product(fVec);
        for (int i = 0; i < retArray.length; i++) {
            retArray[i] = (float)Math.tanh(retArray[i] + b.getValue(i));
        }
        return retArray;
    }

    private static float[] propagateForward(WeightMatrix w, WeightVector b, float[] inArray) {
        float[] retArray = w.product(inArray);
        for (int i = 0; i < retArray.length; i++) {
            retArray[i] = (float)Math.tanh(retArray[i] + b.getValue(i));
        }
        return retArray;
    }

    private static void propagateBackward(FeatureVector fVec, float[] outArray, float[] propArray, float eta, WeightMatrix w, WeightVector b) {
        for (int i = 0; i < outArray.length; i++) {
            float t = (1.0f - outArray[i] * outArray[i]) * propArray[i];

            w.addVectorToRow(i, fVec, t * eta);
            b.setValue(i, b.getValue(i) + t * eta);
        }
    }

    private static float[] propagateBackward(float[] inArray, float[] outArray, float[] propArray, float eta, WeightMatrix w, WeightVector b) {
        float[] retArray = new float[w.getColumnDimensions()];
        for (int i = 0; i < outArray.length; i++) {
            float t = (1.0f - outArray[i] * outArray[i]) * propArray[i];

            for (int c = 0; c < w.getColumnDimensions(); c++) {
                retArray[c] += t * w.getValue(i, c);
            }

            w.addVectorToRow(i, inArray, t * eta);
            b.setValue(i, b.getValue(i) + t * eta);
        }
        return retArray;
    }

    private static float[] outputWithSoftmax(WeightMatrix w, WeightVector b, float[] inArray) {
        float[] retArray = w.product(inArray);
        float expSum = 0.0f;
        for (int i = 0; i < retArray.length; i++) {
            float e = (float)Math.exp(retArray[i] + b.getValue(i));
            retArray[i] = e;
            expSum += e;
        }
        for (int i = 0; i < retArray.length; i++) {
            retArray[i] /= expSum;
        }
        return retArray;
    }

    private static float[] learnByOutputWithSoftmax(float[] inArray, int y, float[] oArray, float eta, WeightMatrix w, WeightVector b) {
        float[] retArray = new float[w.getColumnDimensions()];
        for (int i = 0; i < oArray.length; i++) {
            float p = oArray[i];
            float t = (i == y) ? 1.0f - p : -p;

            for (int c = 0; c < w.getColumnDimensions(); c++) {
                retArray[c] += t * w.getValue(i, c);
            }

            w.addVectorToRow(i, inArray, t * eta);
            b.setValue(i, b.getValue(i) + t * eta);
        }
        return retArray;
    }

    private static void regularize(float eta, float lambda, WeightMatrix w, WeightVector b) {
        w.scale(1.0f - eta * lambda);
        b.scale(1.0f - eta * lambda);
    }

    public static void initializeWeight(WeightMatrix w) {
        Random rnd = new Random(0x5EED);
        float high = (float)(Math.sqrt(6.0 / (w.getRowDimensions() + w.getColumnDimensions())));
        float low = -high;

        for (int r = 0; r < w.getRowDimensions(); r++) {
            for (int c = 0; c < w.getColumnDimensions(); c++) {
                float v = uniformRandom(high, low, rnd);
                w.setValue(r, c, v);
            }
        }
    }

    private static float uniformRandom(float high, float low, Random rnd) {
         return rnd.nextFloat() * (high - low) + low;
    }

}


