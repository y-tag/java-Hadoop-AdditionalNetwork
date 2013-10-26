package myorg.examples.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.ArrayList;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMASGDLearner;

public class SVMASGDTrainInMemoryRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        int dim = 1 << 16;
        float eta0 = 1e-1f;
        float lambda = 1e-4f;
        int numIters = 1000000;

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
        WeightVector weight = new WeightVector(dim);

        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();

        while ((line = reader.readLine()) != null) {
            FeatureVector datum = new FeatureVector();
            SVMLightFormatParser.parse(line, datum);

            data.add(datum);
        }

        SVMASGDLearner.learnWithStochasticLoop(data, eta0, lambda, numIters, weight);

        
        BufferedWriter weightWriter;
        
        if (weightName.endsWith(".gz")) {
            weightWriter = new BufferedWriter(new OutputStreamWriter(
                                        new GZIPOutputStream(new BufferedOutputStream(
                                        new FileOutputStream(weightName)))));
        } else {
            weightWriter = new BufferedWriter(new OutputStreamWriter(
                                        new BufferedOutputStream(
                                        new FileOutputStream(weightName))));
        }

        weightWriter.write(weight.toString());
        weightWriter.flush();
        weightWriter.close();

    }

}
