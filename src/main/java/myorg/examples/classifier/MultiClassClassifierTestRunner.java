package myorg.examples.classifier;

import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;

public class MultiClassClassifierTestRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: test_bin weight_bin");
            return;
        }
        String testBin = args[0];
        String weightBin = args[1];

        WritableCacheReader weightReader = new WritableCacheReader(weightBin);

        WeightVector weight = new WeightVector();
        ArrayList<WeightVector> weightList = new ArrayList<WeightVector>();
        while (weightReader.read(weight) > 0) {
            weightList.add(weight);
            weight = new WeightVector();
        }
        weightReader.close();

        WeightVector[] weightMatrix = new WeightVector[weightList.size()];
        for (int l = 0; l < weightList.size(); l++) {
            weightMatrix[l] = weightList.get(l);
        }

        long num = 0;
        long correct = 0;
        FeatureVector datum = new FeatureVector();

        WritableCacheReader testReader = new WritableCacheReader(testBin);
        while (testReader.read(datum) > 0) {
            int pLabel = -1;
            float maxScore = -Float.MAX_VALUE;
            for (int l = 0; l < weightMatrix.length; l++) {
                float score = weightMatrix[l].innerProduct(datum);
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
