package myorg.util;

import java.util.List;
import java.util.TreeMap;

import com.google.common.hash.Hashing;

public class SVMLightFormatConverter {

    public static String convertTo(List<String> StrList, int bitMask) {
        if (StrList == null) {
            return "";
        }

        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        for (int i = 0; i < StrList.size(); i++) {
            int hash = Hashing.murmur3_32(i).hashString(StrList.get(i)).asInt();
            map.put(hash & bitMask, 1);
        }

        StringBuffer sb = new StringBuffer();
        for (Integer i : map.keySet()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(i);
            sb.append(':');
            sb.append(map.get(i));
        }

        return sb.toString();
    }
}

