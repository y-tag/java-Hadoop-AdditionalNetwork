package myorg.examples.util;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Random;

public class RandomShuffle {

    public static void main(String[] args) throws Exception {

        String tmpDir = "/tmp";

        String line;
        BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

        ArrayList<String> tmpLineArray = new ArrayList<String>();
        ArrayList<String> tmpFileArray = new ArrayList<String>();

        while ((line = inReader.readLine()) != null) {
            tmpLineArray.add(line);

            if (tmpLineArray.size() >= 1000000) {
                Collections.shuffle(tmpLineArray);

                String tmpFile = String.format("%s/mysort%d", tmpDir, tmpFileArray.size());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                                           new FileOutputStream(tmpFile)));
                for (int i = 0; i < tmpLineArray.size(); i++) {
                    writer.write(tmpLineArray.get(i));
                    writer.write("\n");
                }
                writer.close();

                tmpFileArray.add(tmpFile);
                tmpLineArray.clear();
            }
        }
        if (tmpLineArray.size() > 0) {
            Collections.shuffle(tmpLineArray);

            String tmpFile = String.format("%s/mysort%d", tmpDir, tmpFileArray.size());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                                       new FileOutputStream(tmpFile)));
            for (int i = 0; i < tmpLineArray.size(); i++) {
                writer.write(tmpLineArray.get(i));
                writer.write("\n");
            }
            writer.close();

            tmpFileArray.add(tmpFile);
            tmpLineArray.clear();
        }

        ArrayList<BufferedReader> brArray = new ArrayList<BufferedReader>();
        for (int i = 0; i < tmpFileArray.size(); i++) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                                       new FileInputStream(tmpFileArray.get(i))));
            brArray.add(reader);
        }

        Random rnd = new Random();

        while (brArray.size() > 0) {
            int r = rnd.nextInt(brArray.size());
            if ((line = brArray.get(r).readLine()) == null) {
                brArray.remove(r);
                continue;
            }
            System.out.print(String.format("%s\n", line));
        }

        for (int i = 0; i < tmpFileArray.size(); i++) {
            File tmpFile = new File(tmpFileArray.get(i));
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }

}
