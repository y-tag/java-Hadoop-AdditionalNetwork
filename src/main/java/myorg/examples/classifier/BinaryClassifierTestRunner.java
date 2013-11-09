package myorg.examples.classifier;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;

public class BinaryClassifierTestRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: test_bin weight_bin");
            return;
        }
        String testBin = args[0];
        String weightBin = args[1];

        WritableCacheReader weightReader = new WritableCacheReader(weightBin);

        WeightVector weight = new WeightVector();
        weightReader.read(weight);
        weightReader.close();

        long num = 0;
        long correct = 0;
        FeatureVector datum = new FeatureVector();

        WritableCacheReader testReader = new WritableCacheReader(testBin);
        while (testReader.read(datum) > 0) {
            float score = weight.innerProduct(datum);

            num++;
            if (datum.getLabel() * score > 0.0f) {
                correct++;
            }
        }
        testReader.close();

        System.out.println("Accuracy = " + Double.toString(100.0 * correct / num) + "% " + "(" + Long.toString(correct) + "/" + Long.toString(num) + ")");
    }

}
