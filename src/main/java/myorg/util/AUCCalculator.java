package myorg.util;

import java.util.Collections;
import java.util.List;

import myorg.util.ScoreStruct;

public class AUCCalculator {

    public static double calcAUC(List<ScoreStruct> ssList) {
        Collections.sort(ssList);

        double auc = 0.0;
        double oldPosSum = 0.0;
        double posSum = 0.0;
        double negSum = 0.0;
        double negNum = 0.0;

        float lastPredict = Float.MAX_VALUE;

        for (ScoreStruct ss : ssList) {
            if (lastPredict != ss.value) {
                auc += (oldPosSum + posSum) * negNum / 2.0;
                oldPosSum = posSum;
                negNum = 0;
            }

            lastPredict = ss.value;
            negNum += ss.negative;
            negSum += ss.negative;
            posSum += ss.positive;
        }

        auc += (oldPosSum + posSum) * negNum / 2.0;
        auc /= (posSum * negSum);

        return auc;
    }

}
