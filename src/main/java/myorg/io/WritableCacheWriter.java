package myorg.io;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.hadoop.io.Writable;

public class WritableCacheWriter<T extends Writable> {
    private DataOutputStream outStream;

    public WritableCacheWriter(String fileName)
            throws IOException, FileNotFoundException {
        if (fileName.endsWith(".gz")) {
            this.outStream = new DataOutputStream(
                                new GZIPOutputStream(
                                new FileOutputStream(fileName)));
        } else {
            this.outStream = new DataOutputStream(
                                new BufferedOutputStream(
                                new FileOutputStream(fileName)));
        }
    }

    public void write(T w) throws IOException {
        w.write(outStream);
    }

    public void close() throws IOException {
        outStream.close();
    }

}

