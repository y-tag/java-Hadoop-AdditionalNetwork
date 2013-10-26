package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;

public class MaxEntSGDLearner {

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList,
       float eta0, float lambda, int numIters, WeightVector[] wMatrix
    ) {
        if (wMatrix == null) { return; }
        for (int i = 0; i < wMatrix.length; i++) {
            if (wMatrix[i] == null) { return; }
        }

        int dataSize = fVecList.size();

        Random rnd = new Random(0x5EED);

        for (int i = 1; i <= numIters; i++) {
            float eta = eta0 / (1.0f + eta0 * lambda * i);

            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);

            learnWithStochasticOneStep(fVec, eta, lambda, wMatrix);
        }
    }

    public static void learnWithStochasticOneStep(
        FeatureVector fVec, float eta, float lambda, WeightVector[] wMatrix
    ) {
        int y = (int)fVec.getLabel();
        if (y < 0 || wMatrix.length < y) {
            return;
        }

        float[] expArray = new float[wMatrix.length];
        float expSum = 0.0f;

        for (int i = 0; i < expArray.length; i++) {
            WeightVector w = wMatrix[i];
            float e = (float)Math.exp(w.innerProduct(fVec));
            expArray[i] = e;
            expSum += e;
        }

        for (int i = 0; i < expArray.length; i++) {
            WeightVector w = wMatrix[i];
            w.scale(1.0f - eta * lambda);
            float p = expArray[i] / expSum;
            float t = (i == y) ? 1.0f - p : -p;
            w.addVector(fVec, t * eta);
        }
    }
}

