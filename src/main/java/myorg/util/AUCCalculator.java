package myorg.util;

import java.util.Collections;
import java.util.List;

import myorg.util.ScoreStruct;

public class AUCCalculator {

    public static double calcAUC(List<ScoreStruct> ssList) {
        Collections.sort(ssList);

        double auc = 0.0;
        long oldPosSum = 0;
        long posSum = 0;
        long negSum = 0;
        int negNum = 0;

        float lastPredict = Float.MAX_VALUE;

        for (ScoreStruct ss : ssList) {
            if (lastPredict != ss.predict) {
                auc += (oldPosSum + posSum) * negNum / 2.0;
                oldPosSum = posSum;
                negNum = 0;
            }

            lastPredict = ss.predict;
            negNum += ss.negative;
            negSum += ss.negative;
            posSum += ss.positive;
        }

        auc += (oldPosSum + posSum) * negNum / 2.0;
        auc /= (posSum * negSum);

        return auc;
    }

}
