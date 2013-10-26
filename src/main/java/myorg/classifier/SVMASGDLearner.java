package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;

public class SVMASGDLearner {

    public static void learnWithStochasticLoop(
       List<FeatureVector> fVecList,
       float eta0, float lambda, int numIters, WeightVector A
    ) {
        if (A == null) {
            return;
        }

        A.scale(0.0f);
        WeightVector W = new WeightVector(A.getDimensions());

        int dataSize = fVecList.size();

        Random rnd = new Random(0x5EED);

        float[] params = {1.0f, 1.0f, 1.0f};

        for (int i = 1; i <= numIters; i++) {
            int idx = rnd.nextInt(dataSize);
            FeatureVector fVec = fVecList.get(idx);

            float eta = eta0 / (float)(Math.pow(1.0f + eta0 * lambda * i, 0.75));
            float mu = 1.0f / (float)Math.max(1.0, i - dataSize);

            learnWithStochasticOneStep(fVec, eta, lambda, W, A, mu, params);
        }

        A.addVector(W, params[1]);
        A.scale(1.0f / params[2]);
    }

    public static void learnWithStochasticOneStep(
       FeatureVector fVec, float eta, float lambda,
       WeightVector W, WeightVector A, float mu, float[] params
    ) {
        float s     = params[0];
        float alpha = params[1];
        float beta  = params[2];

        float y = (fVec.getLabel() > 0.0f) ? 1.0f : -1.0f;
        float ip = W.innerProduct(fVec);
        float loss = 1.0f - s * y * ip;

        s *= (1.0f - eta * lambda);
        if (loss > 0.0f) {
            W.addVector(fVec,  y * eta / s);
            if (mu < 1.0f) { A.addVector(fVec, -y * eta * alpha / s); }
        }
        if (mu == 1.0f) {
            W.scale(0.0f);
            beta = 1.0f; 
            alpha = s;
        } else {
            beta /= (1.0f - mu);
            alpha += mu * beta * s;
        }

        params[0] = s;
        params[1] = alpha;
        params[2] = beta;
    }
}

