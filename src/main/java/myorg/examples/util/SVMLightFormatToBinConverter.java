package myorg.examples.util;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import myorg.io.FeatureVector;
import myorg.io.WritableCacheWriter;
import myorg.util.SVMLightFormatParser;

public class SVMLightFormatToBinConverter {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: file_name bin_name");
            return;
        }
        String fileName = args[0];
        String binName = args[1];

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

        WritableCacheWriter cacheWriter = new WritableCacheWriter(binName);
        while ((line = reader.readLine()) != null) {
            datum.clear();
            SVMLightFormatParser.parse(line, datum);
            cacheWriter.write(datum);
        }
        cacheWriter.close();
    }

}
