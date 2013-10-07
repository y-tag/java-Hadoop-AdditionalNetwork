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

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMPegasosLearner;

public class SVMPegasosTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        float lambda = 1e-4f;
        String cacheName  = "cache";

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
        FeatureVector datum = new FeatureVector();
        WeightVector weight = new WeightVector(dim);

        long i = 1;
        int numIters = 2;

        WritableCacheWriter cacheWriter = new WritableCacheWriter(cacheName);
        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);
            cacheWriter.write(datum);

            float eta = 1.0f / (lambda * i);
            SVMPegasosLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
            i++;
        }
        cacheWriter.close();

        WritableCacheReader cacheReader = new WritableCacheReader(cacheName);
        for (int n = 1; n < numIters; n++) {
            while (cacheReader.read(datum) > 0) {
                float eta = 1.0f / (lambda * i);
                SVMPegasosLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
                i++;
            }
            cacheReader.reopen();
        }
        cacheReader.close();

        
        File cacheFile = new File(cacheName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }


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
