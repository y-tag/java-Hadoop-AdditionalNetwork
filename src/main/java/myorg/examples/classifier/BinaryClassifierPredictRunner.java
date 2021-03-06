package myorg.examples.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.util.SVMLightFormatParser;

public class BinaryClassifierPredictRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: test_bin weight_bin [use_sigmoid]");
            return;
        }
        String testBin = args[0];
        String weightBin = args[1];

        boolean use_sigmoid = (args.length > 2 && ! args[2].isEmpty()) ? true : false;

        WritableCacheReader<WeightVector> weightReader = new WritableCacheReader<WeightVector>(weightBin);

        WeightVector weight = new WeightVector();
        weightReader.read(weight);
        weightReader.close();

        long num = 0;
        long correct = 0;
        FeatureVector datum = new FeatureVector();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        WritableCacheReader<FeatureVector> testReader = new WritableCacheReader<FeatureVector>(testBin);
        while (testReader.read(datum) > 0) {
            float score = weight.innerProduct(datum);
            if (use_sigmoid) {
                score = 1.0f / (1.0f + (float)Math.exp(-score));
            }
            writer.write(String.format("%f\n", score));
        }
        testReader.close();

        writer.flush();
        writer.close();
    }

}
