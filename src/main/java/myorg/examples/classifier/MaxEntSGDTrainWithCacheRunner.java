package myorg.examples.classifier;

import org.apache.hadoop.io.Writable;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightMatrix;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.MaxEntSGDLearner;

public class MaxEntSGDTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: train_bin weight_bin");
            return;
        }
        String trainBin = args[0];
        String weightBin = args[1];

        int dim = 1 << 16;
        float eta0 = 1e-2f;
        float lambda = 1e-4f;
        int numEpochs= 10;

        int maxLabel = 0;
        FeatureVector datum = new FeatureVector();

        WritableCacheReader<FeatureVector> trainReader = new WritableCacheReader<FeatureVector>(trainBin);
        while (trainReader.read(datum) > 0) {
            if (datum.getLabel() > maxLabel) {
                maxLabel = (int)datum.getLabel();
            }
        }

        WeightMatrix weightMatrix = new WeightMatrix(maxLabel + 1, dim);
        WeightVector biasVector   = new WeightVector(maxLabel + 1);

        int i = 1;
        for (int n = 0; n < numEpochs; n++) {
            trainReader.reopen();
            while (trainReader.read(datum) > 0) {
                float eta = eta0 / (1.0f + eta0 * lambda * i);
                MaxEntSGDLearner.learnWithStochasticOneStep(datum, eta, lambda, weightMatrix, biasVector);
                i++;
            }
        }
        trainReader.close();

        WritableCacheWriter<Writable> weightWriter = new WritableCacheWriter<Writable>(weightBin);
        weightWriter.write(weightMatrix);
        weightWriter.write(biasVector);
        weightWriter.close();
    }

}
