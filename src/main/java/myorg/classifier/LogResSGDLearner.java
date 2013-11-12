package myorg.classifier;

import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;

public class LogResSGDLearner {

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList,
       float eta0, float lambda, int numIters, WeightVector w
    ) {
        if (w == null) {
            return;
        }

        int dataSize = fVecList.size();

        Random rnd = new Random(0x5EED);

        for (int i = 1; i <= numIters; i++) {
            float eta = eta0 / (1.0f + eta0 * lambda * i);

            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);

            learnWithStochasticOneStep(fVec, eta, lambda, w);
        }
    }

    public static void learnWithStochasticOneStep(
       FeatureVector fVec, float eta, float lambda, WeightVector w
    ) {
        float y = (fVec.getLabel() > 0.0f) ? 1.0f : -1.0f;
        float ip = w.innerProduct(fVec);

        //w.scale(1.0f - eta * lambda);

        float z = y * ip;
        float coef = 0.0f;
        if (z < 10.0f) {
            coef = (float)(1.0 / (1.0 + Math.exp(z)));
        } else {
            double ez = Math.exp(-z);
            coef = (float)(ez / (ez + 1.0));
        }

        w.addVector(fVec, y * coef * eta);
    }
}

