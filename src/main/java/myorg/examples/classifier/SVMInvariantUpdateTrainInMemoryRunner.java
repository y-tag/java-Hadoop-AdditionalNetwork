package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.SVMInvariantUpdateLearner;

public class SVMInvariantUpdateTrainInMemoryRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: train_bin weight_bin");
            return;
        }
        String trainBin = args[0];
        String weightBin = args[1];

        int dim = 1 << 24;
        float eta0 = 1e-1f;
        float lambda = 1e-4f;
        int numIters = 1000000;
        WeightVector weight = new WeightVector(dim);

        FeatureVector datum = new FeatureVector();
        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();

        WritableCacheReader trainReader = new WritableCacheReader(trainBin);
        while (trainReader.read(datum) > 0) {
            data.add(datum);
            datum = new FeatureVector();
        }
        trainReader.close();

        SVMInvariantUpdateLearner.learnWithStochasticLoop(data, eta0, lambda, numIters, weight);

        WritableCacheWriter weightWriter = new WritableCacheWriter(weightBin);

        weightWriter.write(weight);
        weightWriter.close();
    }

}
