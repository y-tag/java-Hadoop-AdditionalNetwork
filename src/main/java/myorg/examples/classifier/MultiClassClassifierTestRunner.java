package myorg.examples.classifier;

import java.util.ArrayList;

import org.apache.hadoop.io.Writable;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightMatrix;
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

        WritableCacheReader<Writable> weightReader = new WritableCacheReader<Writable>(weightBin);
        WeightMatrix weightMatrix = new WeightMatrix();
        weightReader.read(weightMatrix);
        WeightVector biasVector = new WeightVector();
        weightReader.read(biasVector);
        weightReader.close();

        long num = 0;
        long correct = 0;
        FeatureVector datum = new FeatureVector();

        WritableCacheReader<FeatureVector> testReader = new WritableCacheReader<FeatureVector>(testBin);
        while (testReader.read(datum) > 0) {
            float[] prodArray = weightMatrix.product(datum);
            int pLabel = -1;
            float maxScore = -Float.MAX_VALUE;
            for (int l = 0; l < prodArray.length; l++) {
                float score = prodArray[l] + biasVector.getValue(l);
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
