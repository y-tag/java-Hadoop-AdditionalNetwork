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
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMDCDLearner;

public class SVMDCDTrainWithCacheRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name weight_name");
            return;
        }
        String fileName = args[0];
        String weightName = args[1];

        float C = 1.0f;
        int maxIters = 10;
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

        SVMDCDLearner.LossType lossType = SVMDCDLearner.LossType.SQUAREDHINGE;
        float U = SVMDCDLearner.getU(C, lossType);
        float D = SVMDCDLearner.getD(C, lossType);
        ArrayList<Float> sNormList = new ArrayList<Float>();
        ArrayList<Float> alphaList = new ArrayList<Float>();
        float[] alpha = new float[1];

        WritableCacheWriter cacheWriter = new WritableCacheWriter(cacheName);
        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);
            cacheWriter.write(datum);

            alpha[0] = 0.0f;
            float sNorm = datum.getSquaredNorm();

            SVMDCDLearner.learnWithOneStep(datum, sNorm, U, D, C, alpha, weight);

            alphaList.add(alpha[0]);
            sNormList.add(sNorm);
        }
        cacheWriter.close();

        WritableCacheReader cacheReader = new WritableCacheReader(cacheName);
        for (int iter = 2; iter <= maxIters; iter++) {
            int i = 0;
            float maxPG = -Float.MAX_VALUE;
            float minPG =  Float.MAX_VALUE;
            float PG = 0.0f;

            while (cacheReader.read(datum) > 0) {
                alpha[0] = alphaList.get(i);
                float sNorm = sNormList.get(i);

                PG = SVMDCDLearner.learnWithOneStep(datum, sNorm, U, D, C, alpha, weight);

                alphaList.set(i, alpha[0]);
                maxPG = Math.max(PG, maxPG);
                minPG = Math.min(PG, minPG);
                i++;
            }
            cacheReader.reopen();

            if (maxPG - minPG < 1.0e-6) {
                break;
            }
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
