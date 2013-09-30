package myorg.util;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import myorg.io.FeatureVector;

public class SVMLightFormatParser {

    public static void parse(String inString, FeatureVector fVec) {
        parse(inString, fVec, false);
    }

    public static void parse(String inString, FeatureVector fVec, boolean isBiasTermUsed) {
        String strLabel = parseForStrLabel(inString, fVec, isBiasTermUsed);
        try {
            fVec.setLabel(Float.parseFloat(strLabel));
        } catch (NumberFormatException e) {
        }
    }

    public static String parseForStrLabel(String inString, FeatureVector fVec) {
        return parseForStrLabel(inString, fVec, false);
    }

    public static String parseForStrLabel(String inString, FeatureVector fVec, boolean isBiasTermUsed) {
        if (inString == null) {
            return "";
        }

        int idx = inString.indexOf('#'); // comment part
        String substr = (idx > 0) ? inString.substring(0, idx) : inString;

        StringTokenizer st = new StringTokenizer(substr, " \t\r\n:");
        if (! st.hasMoreTokens()) {
            return "";
        }

        String label = st.nextToken();
        if (fVec == null) {
            return label;
        }
        fVec.clear();

        Map<Integer, Float> tmpMap = new HashMap<Integer, Float>();
        if (isBiasTermUsed) {
            tmpMap.put(0, 1.0f);
        }

        while (st.hasMoreTokens()) {
            String key = st.nextToken();

            if (! st.hasMoreTokens()) {
                break;
            }

            String val = st.nextToken();

            try {
                int k = Integer.parseInt(key);
                float v = Float.parseFloat(val);
                tmpMap.put(k, v);
            } catch (NumberFormatException e) {
            }
        }

        fVec.set(tmpMap);

        return label;
    }
}

