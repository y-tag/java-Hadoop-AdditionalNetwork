package myorg.examples.classifier;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.util.SVMLightFormatParser;

public class ClassifierTestRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        BufferedReader weightReader;

        if (weightName.endsWith(".gz")) {
            weightReader = new BufferedReader(new InputStreamReader(
                                              new GZIPInputStream(new BufferedInputStream(
                                              new FileInputStream(weightName)))));
        } else {
            weightReader = new BufferedReader(new InputStreamReader(
                                             new BufferedInputStream(
                                             new FileInputStream(weightName))));
        }

        WeightVector weight = new WeightVector(weightReader.readLine());
        weightReader.close();

        BufferedReader reader;
        
        if (fileName.endsWith(".gz")) {
            reader = new BufferedReader(new InputStreamReader(
                                        new GZIPInputStream(new BufferedInputStream(
                                        new FileInputStream(fileName)))));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                                        new BufferedInputStream(
                                        new FileInputStream(fileName))));
        }

        String line;
        FeatureVector datum = new FeatureVector();

        long num = 0;
        long correct = 0;

        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);

            float score = weight.innerProduct(datum);

            num++;
            if (datum.getLabel() * score > 0.0f) {
                correct++;
            }
        }
        reader.close();

        System.out.println("Accuracy = " + Double.toString(100.0 * correct / num) + "% " + "(" + Long.toString(correct) + "/" + Long.toString(num) + ")");

    }

}
