package myorg.classifier;

import java.lang.Math;
import java.util.List;
import java.util.Random;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;

public class SVMDCDLearner {

    public enum LossType {
        HINGE, SQUAREDHINGE
    }

    public static void learnWithLoop(
       List<FeatureVector> fVecList,
       float C, LossType lossType, int maxIters, WeightVector w
    ) {
        if (w == null) {
            return;
        }

        Random rnd = new Random(0x5EED);

        int dataSize = fVecList.size();
        int[] idxArray = new int[dataSize];
        float[] alphaArray = new float[dataSize];
        float[] sNormArray = new float[dataSize];

        for (int i = 0; i < dataSize; i++) {
            idxArray[i] = i;
            alphaArray[i] = 0.0f;
            sNormArray[i] = fVecList.get(i).getSquaredNorm();
        }

        float U = getU(C, lossType);
        float D = getD(C, lossType);
        float[] alpha = new float[1];

        for (int iter = 1; iter <= maxIters; iter++) {
            float maxPG = -Float.MAX_VALUE;
            float minPG =  Float.MAX_VALUE;
            float PG = 0.0f;

            for (int i = 0; i < dataSize; i++) {
                int j = i + rnd.nextInt(dataSize - i);
                int idx = idxArray[j];
                idxArray[j] = idxArray[i];
                idxArray[i] = idx;

                alpha[0] = alphaArray[idx];
                PG = learnWithOneStep(fVecList.get(idx), sNormArray[idx], U, D,
                                      C, alpha, w);
                alphaArray[idx] = alpha[0];

                maxPG = Math.max(PG, maxPG);
                minPG = Math.min(PG, minPG);
            }

            if (maxPG - minPG < 1.0e-6) {
                break;
            }
        }
    }

    public static float learnWithOneStep(
       FeatureVector fVec, float sNorm, float U, float D,
       float C, float[] alpha, WeightVector w
    ) {
        float y = (fVec.getLabel() > 0) ? 1.0f : -1.0f;
        float ip = w.innerProduct(fVec);

        float oldAlpha = (alpha != null && alpha.length > 0) ? alpha[0] : 0.0f;
        float G = y * ip - 1.0f + D * oldAlpha;

        float PG = G;
        if      (oldAlpha == 0.0f) { PG = Math.min(G, 0.0f); }
        else if (oldAlpha == U)    { PG = Math.max(G, 0.0f); }
        
        if (Math.abs(PG) > 1.0e-10) {
            float Q = sNorm + D;
            float newAlpha = Math.min(Math.max(oldAlpha - G/Q, 0.0f), U);
            w.addVector(fVec, y * (newAlpha - oldAlpha));
            if (alpha != null && alpha.length > 0) { alpha[0] = newAlpha; }
        }

        return PG;
    }

    public static float getU(float C, LossType lossType) {
        if (lossType == LossType.SQUAREDHINGE) {
            return Float.MAX_VALUE;
        }
        return C;
    }

    public static float getD(float C, LossType lossType) {
        if (lossType == LossType.SQUAREDHINGE) {
            return 0.5f / C;
        }
        return 0.0f;
    }

}

