package myorg.examples.util;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import myorg.util.ScoreStruct;
import myorg.util.AUCCalculator;

public class KDDCup2012Track2Scorer {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: solution_file prediction_file");
            return;
        }
        String solutionFile = args[0];
        String predictionFile = args[1];

        BufferedReader solutionReader = new BufferedReader(new InputStreamReader(
                                        new BufferedInputStream(
                                        new FileInputStream(solutionFile))));;
        BufferedReader predictionReader = new BufferedReader(new InputStreamReader(
                                          new BufferedInputStream(
                                          new FileInputStream(predictionFile))));;
        
        String line;
        ArrayList<ScoreStruct> ssList = new ArrayList<ScoreStruct>();
        StringTokenizer st;

        long solutionNum = 0;
        long predictionNum = 0;

        while ((line = solutionReader.readLine()) != null) {
            st = new StringTokenizer(line, ",");
            int tokensNum = st.countTokens();

            if (tokensNum < 2) { continue; }
            int clicks = 0;
            int imps   = 0;

            try {
                clicks = Integer.parseInt(st.nextToken());
                imps   = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
                //System.err.println(String.format("skip '%s' in solution file", line));
                continue;
            }

            float prediction = 0.0f;

            while ((line = predictionReader.readLine()) != null) {
                int p = line.indexOf(',');
                if (p > 0) { line = line.substring(0, p); }

                try {
                    prediction = Float.parseFloat(line);
                } catch (Exception e) {
                    //System.err.println(String.format("skip '%s' in prediction file", line));
                    continue;
                }

                break;
            }

            if (line == null) { break; }

            ScoreStruct ss = new ScoreStruct();
            ss.predict = prediction;
            ss.positive = clicks;
            ss.negative = imps - clicks;

            ssList.add(ss);

            solutionNum += 1;
            predictionNum += 1;
        }

        while ((line = solutionReader.readLine()) != null) {
            solutionNum += 1;
        }
        while ((line = predictionReader.readLine()) != null) {
            predictionNum += 1;
        }
        solutionReader.close();
        predictionReader.close();

        //System.err.println(String.format("#solutions:   %d", solutionNum));
        //System.err.println(String.format("#predictions: %d", predictionNum));

        if (solutionNum != predictionNum) {
            throw new RuntimeException("number of lines are not the same!");
        }

        double auc = AUCCalculator.calcAUC(ssList);

        double wrmse = 0.0;
        double nwmae = 0.0;
        double weightSum = 0.0;

        for (ScoreStruct ss : ssList) {
            double imps = ss.positive + ss.negative;
            double diff = (ss.positive / imps) - ss.predict;
            nwmae += Math.abs(diff) * imps;
            wrmse += Math.pow(diff, 2.0) * imps;
            weightSum += imps;
        }
        nwmae /= weightSum;
        wrmse = Math.sqrt(wrmse / weightSum);

        System.out.println(String.format("AUC  : %f", auc));
        System.out.println(String.format("NWMAE: %f", nwmae));
        System.out.println(String.format("WRMSE: %f", wrmse));
    }
}

