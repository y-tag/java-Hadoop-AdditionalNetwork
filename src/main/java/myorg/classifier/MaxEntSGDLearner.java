package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightMatrix;

public class MaxEntSGDLearner {

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList,
       float eta0, float lambda, int numIters, WeightMatrix wMatrix
    ) {
        if (wMatrix == null) { return; }

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
        FeatureVector fVec, float eta, float lambda, WeightMatrix wMatrix
    ) {
        int y = (int)fVec.getLabel();
        if (y < 0 || wMatrix.getRowDimensions() < y) {
            return;
        }

        float[] expArray = wMatrix.product(fVec);
        float expSum = 0.0f;
        for (int i = 0; i < expArray.length; i++) {
            float e = (float)Math.exp(expArray[i]);
            expArray[i] = e;
            expSum += e;
        }

        wMatrix.scale(1.0f - eta * lambda);

        for (int i = 0; i < expArray.length; i++) {
            float p = expArray[i] / expSum;
            float t = (i == y) ? 1.0f - p : -p;
            wMatrix.addVectorToRow(i, fVec, t * eta);
        }
    }
}

