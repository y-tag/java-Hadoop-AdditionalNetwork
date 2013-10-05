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

import myorg.io.WeightVector;

public class WeightVectorTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
 
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testToString() throws IOException {

        int dim = 1024 * 1024;
        double epsilon = 1e-5;
        Random random = new Random();

        WeightVector w1 = new WeightVector(dim);

        for (int i = 0; i < 1000; i++) {
            int k = Math.abs(random.nextInt()) % dim;
            float v = random.nextFloat();
            w1.setValue(k, v);
        }

        WeightVector w2 = new WeightVector(w1.toString());

        assertEquals(w1.getDimensions(), w2.getDimensions());

        for (int i = 0; i < w2.getDimensions(); i++) {
            assertEquals(w1.getValue(i), w2.getValue(i), epsilon);
        }
    }
}


