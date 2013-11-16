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

import myorg.io.WeightMatrix;

public class WeightMatrixTest {

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

        int row = 256;
        int col = 1024;
        double epsilon = 1e-5;
        Random random = new Random();

        WeightMatrix w1 = new WeightMatrix(row, col);

        for (int i = 0; i < 1000; i++) {
            int r = Math.abs(random.nextInt()) % row;
            int c = Math.abs(random.nextInt()) % col;
            float v = random.nextFloat();
            w1.setValue(r, c, v);
        }

        WeightMatrix w2 = new WeightMatrix(w1.toString());

        assertEquals(row, w2.getRowDimensions());
        assertEquals(col, w2.getColumnDimensions());

        for (int r = 0; r < w2.getRowDimensions(); r++) {
            for (int c = 0; c < w2.getColumnDimensions(); c++) {
                assertEquals(w1.getValue(r, c), w2.getValue(r, c), epsilon);
            }
        }
    }

    @Test
    public void testScale() throws IOException {

        int row = 256;
        int col = 1024;
        double epsilon = 1e-5;
        Random random = new Random();

        WeightMatrix w1 = new WeightMatrix(row, col);
        WeightMatrix w2 = new WeightMatrix(row, col);
        WeightMatrix w3 = new WeightMatrix(row, col);
        float scaleFactor = random.nextFloat();

        for (int i = 0; i < 1000; i++) {
            int r = Math.abs(random.nextInt()) % row;
            int c = Math.abs(random.nextInt()) % col;
            float v = random.nextFloat();
            w1.setValue(r, c, v);
            w2.setValue(r, c, v * scaleFactor);
            w3.setValue(r, c, v);
        }

        w3.scale(scaleFactor);

        assertEquals(w1.getRowDimensions(), w2.getRowDimensions());
        assertEquals(w1.getRowDimensions(), w2.getRowDimensions());
        assertEquals(w1.getColumnDimensions(), w3.getColumnDimensions());
        assertEquals(w1.getColumnDimensions(), w3.getColumnDimensions());

        for (int r = 0; r < w1.getRowDimensions(); r++) {
            for (int c = 0; c < w1.getColumnDimensions(); c++) {
                assertEquals(w1.getValue(r, c) * scaleFactor, w2.getValue(r, c), epsilon);
                assertEquals(w1.getValue(r, c) * scaleFactor, w3.getValue(r, c), epsilon);
            }
        }
    }

}


