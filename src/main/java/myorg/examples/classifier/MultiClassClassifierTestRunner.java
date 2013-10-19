package myorg.examples.classifier;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.util.SVMLightFormatParser;

public class MultiClassClassifierTestRunner {

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

        String line;

        ArrayList<WeightVector> weightList = new ArrayList<WeightVector>();
        while ((line = weightReader.readLine()) != null) {
            WeightVector w = new WeightVector(line);
            weightList.add(w);
        }
        weightReader.close();

        WeightVector[] weightMatrix = new WeightVector[weightList.size()];
        for (int l = 0; l < weightList.size(); l++) {
            weightMatrix[l] = weightList.get(l);
        }

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

        FeatureVector datum = new FeatureVector();

        long num = 0;
        long correct = 0;

        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);

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
        reader.close();

        System.out.println("Accuracy = " + Double.toString(100.0 * correct / num) + "% " + "(" + Long.toString(correct) + "/" + Long.toString(num) + ")");

    }

}
