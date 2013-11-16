package myorg.examples.util;

import myorg.io.WeightMatrix;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMPegasosLearner;

public class WeightMatrixFromBinConverter {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: weight_bin");
            return;
        }
        String weightBin = args[0];

        WritableCacheReader weightReader = new WritableCacheReader(weightBin);

        WeightMatrix wMatrix = new WeightMatrix();
        while (weightReader.read(wMatrix) > 0) {
            System.out.println(wMatrix.toString());
        }
        weightReader.close();
    }

}
