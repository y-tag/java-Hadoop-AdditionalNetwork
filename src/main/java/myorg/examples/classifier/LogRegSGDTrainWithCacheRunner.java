package myorg.examples.classifier;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.LogRegSGDLearner;

public class LogRegSGDTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: train_bin weight_bin");
            return;
        }
        String trainBin = args[0];
        String weightBin = args[1];

        int dim = 1 << 24;
        float eta0 = 1e-1f;
        float lambda = 1e-6f;
        int numEpochs = 2;
        FeatureVector datum = new FeatureVector();
        WeightVector weight = new WeightVector(dim);

        long i = 1;
        WritableCacheReader<FeatureVector> trainReader = new WritableCacheReader<FeatureVector>(trainBin);
        for (int n = 0; n < numEpochs; n++) {
            while (trainReader.read(datum) > 0) {
                float eta = eta0 / (1.0f + eta0 * lambda * i);
                LogRegSGDLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
                i++;
            }
            trainReader.reopen();
        }
        trainReader.close();

        WritableCacheWriter<WeightVector> weightWriter = new WritableCacheWriter<WeightVector>(weightBin);

        weightWriter.write(weight);
        weightWriter.close();
    }

}
