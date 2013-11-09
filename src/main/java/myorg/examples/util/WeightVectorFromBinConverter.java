package myorg.examples.util;

import myorg.io.WeightVector;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;
import myorg.classifier.SVMPegasosLearner;

public class WeightVectorFromBinConverter {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: weight_bin");
            return;
        }
        String weightBin = args[0];

        WritableCacheReader weightReader = new WritableCacheReader(weightBin);

        WeightVector weight = new WeightVector();
        while (weightReader.read(weight) > 0) {
            System.out.println(weight.toString());
        }
        weightReader.close();
    }

}
