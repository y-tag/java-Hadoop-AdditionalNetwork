package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;

public class SVMInvariantUpdateLearner {

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList,
       float eta0, float lambda, int numIters, WeightVector w
    ) {
        if (w == null) {
            return;
        }

        int dataSize = fVecList.size();

        Random rnd = new Random(1000);

        for (int i = 1; i <= numIters; i++) {
            float eta = eta0 / (1.0f + eta0 * lambda * i);
            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);

            learnWithStochasticOneStep(fVec, eta, lambda, w);
        }
    }

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList, List<Float> importanceList,
       float eta0, float lambda, int numIters, WeightVector w
    ) {
        if (w == null) {
            return;
        } else if (fVecList.size() != importanceList.size()) {
            return;
        }

        int dataSize = fVecList.size();

        Random rnd = new Random(1000);

        for (int i = 1; i <= numIters; i++) {
            float eta = eta0 / (1.0f + eta0 * lambda * i);
            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);
            float importance = importanceList.get(idx).floatValue();

            learnWithStochasticOneStep(fVec, importance, eta, lambda, w);
        }
    }

    public static void learnWithStochasticOneStep(
       FeatureVector fVec, float eta, float lambda, WeightVector w
    ) {
        learnWithStochasticOneStep(fVec, 1.0f, eta, lambda, w);
    }

    public static void learnWithStochasticOneStep(
       FeatureVector fVec, float importance, float eta, float lambda, WeightVector w
    ) {
        float y = (fVec.getLabel() > 0.0f) ? 1.0f : -1.0f;
        float ip = w.innerProduct(fVec);

        float loss = 1.0f - y * ip;
        float s = Math.min(importance * eta, loss / fVec.getSquaredNorm());

        w.addVector(fVec, y * s);
        w.scale(1.0f / (1.0f + importance * eta * lambda));
    }
}

