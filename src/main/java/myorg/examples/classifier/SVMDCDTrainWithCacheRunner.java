package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.SVMDCDLearner;

public class SVMDCDTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: train_bin weight_bin");
            return;
        }
        String trainBin = args[0];
        String weightBin = args[1];

        int dim = 1 << 24;
        float C = 1.0f;
        int maxEpochs = 10;
        FeatureVector datum = new FeatureVector();
        WeightVector weight = new WeightVector(dim);

        SVMDCDLearner.LossType lossType = SVMDCDLearner.LossType.SQUAREDHINGE;
        float U = SVMDCDLearner.getU(C, lossType);
        float D = SVMDCDLearner.getD(C, lossType);
        ArrayList<Float> sNormList = new ArrayList<Float>();
        ArrayList<Float> alphaList = new ArrayList<Float>();
        float[] alpha = new float[1];

        WritableCacheReader<FeatureVector> trainReader = new WritableCacheReader<FeatureVector>(trainBin);
        while (trainReader.read(datum) > 0) {
            alpha[0] = 0.0f;
            float sNorm = datum.getSquaredNorm();

            SVMDCDLearner.learnWithOneStep(datum, sNorm, U, D, C, alpha, weight);

            alphaList.add(alpha[0]);
            sNormList.add(sNorm);
        }
        trainReader.reopen();

        for (int epoch = 1; epoch < maxEpochs; epoch++) {
            int i = 0;
            float maxPG = -Float.MAX_VALUE;
            float minPG =  Float.MAX_VALUE;
            float PG = 0.0f;

            while (trainReader.read(datum) > 0) {
                alpha[0] = alphaList.get(i);
                float sNorm = sNormList.get(i);

                PG = SVMDCDLearner.learnWithOneStep(datum, sNorm, U, D, C, alpha, weight);

                alphaList.set(i, alpha[0]);
                maxPG = Math.max(PG, maxPG);
                minPG = Math.min(PG, minPG);
                i++;
            }
            trainReader.reopen();

            if (maxPG - minPG < 1.0e-6) {
                break;
            }
        }
        trainReader.close();

        WritableCacheWriter<WeightVector> weightWriter = new WritableCacheWriter<WeightVector>(weightBin);

        weightWriter.write(weight);
        weightWriter.close();
    }

}
