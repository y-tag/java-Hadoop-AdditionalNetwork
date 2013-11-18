package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightMatrix;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;

import myorg.classifier.MultiLayerPerceptronSGDLearner;

public class MultiLayerPerceptronTestRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: test_bin weight_bin");
            return;
        }
        String testBin = args[0];
        String weightBin = args[1];

        ArrayList<WeightMatrix> wList = new ArrayList<WeightMatrix>();
        ArrayList<WeightVector> bList = new ArrayList<WeightVector>();

        WritableCacheReader weightReader = new WritableCacheReader(weightBin);
        while (true) {
            WeightMatrix w = new WeightMatrix();
            WeightVector b = new WeightVector();
            if (weightReader.read(w) < 0) { break; }
            if (weightReader.read(b) < 0) { break; }
            wList.add(w); bList.add(b);
        }
        weightReader.close();

        long num = 0;
        long correct = 0;
        FeatureVector datum = new FeatureVector();

        WritableCacheReader testReader = new WritableCacheReader(testBin);
        while (testReader.read(datum) > 0) {
            float[] predArray = MultiLayerPerceptronSGDLearner.predict(datum, wList, bList);
            int pLabel = -1;
            float maxScore = -Float.MAX_VALUE;
            for (int l = 0; l < predArray.length; l++) {
                float score = predArray[l];
                if (score > maxScore) {
                    maxScore = score;
                    pLabel = l;
                }
            }

            num++;
            if (datum.getLabel() == pLabel) {
                correct++;
            }
        }
        testReader.close();

        System.out.println("Accuracy = " + Double.toString(100.0 * correct / num) + "% " + "(" + Long.toString(correct) + "/" + Long.toString(num) + ")");

    }

}
