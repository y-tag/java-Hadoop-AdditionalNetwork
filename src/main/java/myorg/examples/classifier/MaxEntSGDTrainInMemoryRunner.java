package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.MaxEntSGDLearner;

public class MaxEntSGDTrainInMemoryRunner {

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
        int numIters = 1000000;

        FeatureVector datum = new FeatureVector();
        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();
        int maxLabel = 0;

        WritableCacheReader trainReader = new WritableCacheReader(trainBin);
        while (trainReader.read(datum) > 0) {
            if (datum.getLabel() > maxLabel) {
                maxLabel = (int)datum.getLabel();
            }

            data.add(datum);
            datum = new FeatureVector();
        }
        trainReader.close();

        WeightVector[] weightMatrix = new WeightVector[maxLabel + 1];
        for (int i = 0; i < weightMatrix.length; i++) {
            weightMatrix[i] = new WeightVector(dim);
        }

        MaxEntSGDLearner.learnWithStochasticLoop(data, eta0, lambda, numIters, weightMatrix);
        
        WritableCacheWriter weightWriter = new WritableCacheWriter(weightBin);

        for (int i = 0; i < weightMatrix.length; i++) {
            weightWriter.write(weightMatrix[i]);
        }
        weightWriter.close();
    }

}
