package myorg.examples.classifier;

import java.util.ArrayList;

import org.apache.hadoop.io.Writable;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightMatrix;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.MultiLayerPerceptronSGDLearner;

public class OneHiddenLayerPerceptronSGDTrainInMemoryRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: train_bin weight_bin");
            return;
        }
        String trainBin = args[0];
        String weightBin = args[1];

        int dim1 = 1 << 10;
        int dim2 = 1 << 9;
        float eta0 = 1e-2f;
        float lambda = 1e-4f;
        int numIters = 100000;

        FeatureVector datum = new FeatureVector();
        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();
        int maxLabel = 0;

        WritableCacheReader<FeatureVector> trainReader = new WritableCacheReader<FeatureVector>(trainBin);
        while (trainReader.read(datum) > 0) {
            if (datum.getLabel() > maxLabel) {
                maxLabel = (int)datum.getLabel();
            }

            data.add(datum);
            datum = new FeatureVector();
        }
        trainReader.close();

        WeightMatrix w1 = new WeightMatrix(dim2, dim1);
        WeightVector b1 = new WeightVector(dim2);
        WeightMatrix w2 = new WeightMatrix(maxLabel + 1, dim2);
        WeightVector b2 = new WeightVector(maxLabel + 1);

        MultiLayerPerceptronSGDLearner.initializeWeight(w1);
        MultiLayerPerceptronSGDLearner.initializeWeight(w2);

        ArrayList<WeightMatrix> wList = new ArrayList<WeightMatrix>();
        ArrayList<WeightVector> bList = new ArrayList<WeightVector>();
        wList.add(w1); bList.add(b1);
        wList.add(w2); bList.add(b2);

        MultiLayerPerceptronSGDLearner.learnWithStochasticLoop(data, eta0, lambda, numIters, wList, bList);
        
        WritableCacheWriter<Writable> weightWriter = new WritableCacheWriter<Writable>(weightBin);
        weightWriter.write(w1); weightWriter.write(b1);
        weightWriter.write(w2); weightWriter.write(b2);
        weightWriter.close();
    }

}
