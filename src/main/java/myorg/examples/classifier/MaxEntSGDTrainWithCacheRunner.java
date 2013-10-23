package myorg.examples.classifier;

import java.io.File;
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
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.MaxEntSGDLearner;

public class MaxEntSGDTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        int dim = 1 << 16;
        float eta0 = 1e-2f;
        float lambda = 1e-4f;
        int numEpochs= 10;
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

        int maxLabel = 0;
        String line;
        FeatureVector datum = new FeatureVector();

        WritableCacheWriter cacheWriter = new WritableCacheWriter(cacheName);
        while ((line = reader.readLine()) != null) {
            SVMLightFormatParser.parse(line, datum);
            cacheWriter.write(datum);

            if (datum.getLabel() > maxLabel) {
                maxLabel = (int)datum.getLabel();
            }
        }
        cacheWriter.close();

        WeightVector[] weightMatrix = new WeightVector[maxLabel + 1];
        for (int r = 0; r < weightMatrix.length; r++) {
            weightMatrix[r] = new WeightVector(dim);
        }

        int i = 1;
        WritableCacheReader cacheReader = new WritableCacheReader(cacheName);
        for (int n = 0; n < numEpochs; n++) {
            while (cacheReader.read(datum) > 0) {
                float eta = eta0 / (1.0f + eta0 * lambda * i);
                MaxEntSGDLearner.learnWithStochasticOneStep(datum, eta, lambda, weightMatrix);
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

        for (int r = 0; r < weightMatrix.length; r++) {
            weightWriter.write(weightMatrix[r].toString() + "\n");
        }
        weightWriter.flush();
        weightWriter.close();

    }

}
