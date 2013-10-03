package myorg.examples.classifier;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMLearner;

public class SVMTrainRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        float lambda = 0.1f;
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

        long i = 0;

        WritableCacheWriter cacheWriter = new WritableCacheWriter(cacheName);
        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);
            cacheWriter.write(datum);

            float eta = 1.0f / (lambda * i);
            SVMLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
            i++;
        }
        cacheWriter.close();

        WritableCacheReader cacheReader = new WritableCacheReader(cacheName);
        while (cacheReader.read(datum) > 0) {
            float eta = 1.0f / (lambda * i);
            SVMLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
            i++;
        }
        cacheReader.close();

        WritableCacheWriter weightWriter = new WritableCacheWriter(weightName);
        weightWriter.write(weight);
        weightWriter.close();
    }

}
