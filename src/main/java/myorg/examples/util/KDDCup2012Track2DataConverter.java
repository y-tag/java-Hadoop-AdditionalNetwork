package myorg.examples.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import myorg.util.SVMLightFormatConverter;

public class KDDCup2012Track2DataConverter {

    public static void main(String[] args) throws Exception {

        int bitMask = (1 << 24) - 1;

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        while ((line = reader.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);

            int tokensNum = st.countTokens();
            int pos = 1;
            int neg = 0;

            if (tokensNum == 12) {
                pos = Integer.parseInt(st.nextToken());
                neg = Integer.parseInt(st.nextToken()) - pos;
                tokensNum -= 2;
            }

            ArrayList<String> tokenList = new ArrayList<String>();

            if (tokensNum == 10) {
                while (st.hasMoreTokens()) {
                    tokenList.add(st.nextToken());
                }
            } else {
                continue;
            }

            String body = SVMLightFormatConverter.convertTo(tokenList, bitMask);

            for (int i = 0; i < pos; i++) {
                System.out.println("+1 " + body);
            }
            for (int i = 0; i < neg; i++) {
                System.out.println("-1 " + body);
            }
        }

    }

}
