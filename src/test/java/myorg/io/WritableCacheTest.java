package myorg.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.io.IntWritable;

import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;

public class WritableCacheTest {

    private String cacheFileName = "cache.tmp.gz";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
 
    @Before
    public void setUp() throws Exception {
        File cacheFile = new File(cacheFileName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }

    @After
    public void tearDown() throws Exception {
        File cacheFile = new File(cacheFileName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }

    @Test
    public void testWithIntWritable() throws IOException {

        Random random = new Random();

        List<IntWritable> wList = new ArrayList<IntWritable>();
        for (int i = 0; i < 1000; i++) {
            wList.add(new IntWritable(random.nextInt()));
        }

        WritableCacheWriter<IntWritable> writer = new WritableCacheWriter<IntWritable>(cacheFileName);

        for (int i = 0; i < wList.size(); i++) {
            writer.write(wList.get(i));
        }
        writer.close();

        WritableCacheReader<IntWritable> reader = new WritableCacheReader<IntWritable>(cacheFileName);

        IntWritable w = new IntWritable();

        for (int i = 0; i < 5; i++) {

            int readNum = 0;
            while (reader.read(w) > 0) {
                w.equals(wList.get(readNum));
                assertEquals(w, wList.get(readNum));
                readNum++;
            }
            assertEquals(readNum, wList.size());

            reader.reopen();
        }
        reader.close();
    }
}

