package myorg.examples.util;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import myorg.util.SVMLightFormatConverter;

public class KDDCup2012Track2DataConverter {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: data_file user_file");
            return;
        }
        String dataFile = args[0];
        String userFile = args[1];

        int bitMask = (1 << 24) - 1;

        String line;
        BufferedReader dataReader;
        BufferedReader userReader;

        if (dataFile.endsWith(".gz")) {
            dataReader = new BufferedReader(new InputStreamReader(
                                            new GZIPInputStream(
                                            new FileInputStream(dataFile))));
        } else {
            dataReader = new BufferedReader(new InputStreamReader(
                                            new FileInputStream(dataFile)));
        }

        if (userFile.endsWith(".gz")) {
            userReader = new BufferedReader(new InputStreamReader(
                                            new GZIPInputStream(
                                            new FileInputStream(userFile))));
        } else {
            userReader = new BufferedReader(new InputStreamReader(
                                            new FileInputStream(userFile)));
        }
        
        HashMap<String, String> genderMap = new HashMap<String, String>();
        HashMap<String, String> ageMap = new HashMap<String, String>();

        int userCap = 24 * 1000 * 1000;
        byte[] genderArray = new byte[userCap];
        byte[] ageArray    = new byte[userCap];

        while ((line = userReader.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            int tokensNum = st.countTokens();

            if (tokensNum != 3) {
                continue;
            }

            int userId = Integer.parseInt(st.nextToken());
            byte gender = Byte.parseByte(st.nextToken());
            byte age = Byte.parseByte(st.nextToken());

            genderArray[userId] = gender;
            ageArray[userId] = age;
        }

        System.err.println("read user data");

        while ((line = dataReader.readLine()) != null) {
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

            int userId = Integer.parseInt(tokenList.get(9));
            String gender = (userId < genderArray.length) ? Byte.toString(genderArray[userId]) : "0";
            String age = (userId < ageArray.length) ? Byte.toString(ageArray[userId]) : "0";

            tokenList.add(gender);
            tokenList.add(age);

            String body = SVMLightFormatConverter.convertTo(tokenList, bitMask);

            for (int i = 0; i < pos; i++) {
                System.out.print(String.format("+1 %s\n", body));
            }
            for (int i = 0; i < neg; i++) {
                System.out.print(String.format("-1 %s\n", body));
            }
        }

    }

}
