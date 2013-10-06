package myorg.examples.classifier;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMPegasosLearner;

public class SVMPegasosTrainInMemoryRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        float lambda = 0.1f;
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

        int dim = 1 << 24;
        String line;
        WeightVector weight = new WeightVector(dim);

        ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();

        while ((line = reader.readLine()) != null) {
            FeatureVector datum = new FeatureVector();
            SVMLightFormatParser.parse(line, datum);

            data.add(datum);
        }

        SVMPegasosLearner.learnWithStochasticLoop(data, lambda, numIters, weight);

        
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
