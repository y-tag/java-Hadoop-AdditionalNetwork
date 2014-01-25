package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.SVMDCDLearner;

public class SVMDCDTrainInMemoryRunner {

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
        WeightVector weight = new WeightVector(dim);

        FeatureVector datum = new FeatureVector();
        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();

        WritableCacheReader<FeatureVector> trainReader = new WritableCacheReader<FeatureVector>(trainBin);
        while (trainReader.read(datum) > 0) {
            data.add(datum);
            datum = new FeatureVector();
        }
        trainReader.close();

        SVMDCDLearner.learnWithLoop(data, C, SVMDCDLearner.LossType.SQUAREDHINGE, maxEpochs, weight);

        WritableCacheWriter<WeightVector> weightWriter = new WritableCacheWriter<WeightVector>(weightBin);

        weightWriter.write(weight);
        weightWriter.close();
    }

}
