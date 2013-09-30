package myorg.io;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.io.Writable;

public class WritableCacheReader<T extends Writable> {
    private String fileName;
    private DataInputStream inStream;

    public WritableCacheReader(String fileName)
            throws IOException, FileNotFoundException {
        open(fileName);
    }

    public void open(String fileName)
            throws IOException, FileNotFoundException {
        if (fileName.endsWith(".gz")) {
            this.inStream = new DataInputStream(
                                new GZIPInputStream(
                                new FileInputStream(fileName)));
        } else {
            this.inStream = new DataInputStream(
                                new BufferedInputStream(
                                new FileInputStream(fileName)));
        }
        this.fileName = fileName;
    }

    public void reopen()
            throws IOException, FileNotFoundException {
        close();
        open(this.fileName);
    }

    public int read(T w) {
        try {
            w.readFields(inStream);
            return 1;
        } catch (IOException e) {
        }
        return -1;
    }

    public void close() throws IOException {
        inStream.close();
    }

}

