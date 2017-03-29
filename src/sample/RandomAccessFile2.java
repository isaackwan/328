package sample; /**
 * Created by isaac on 3/27/17.
 */

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFile2 extends RandomAccessFile {
    RandomAccessFile2(String name, String mode) throws IOException {
        super(name, mode);
    }
    public int readLittleEndian(int bytes) throws IOException {
        int rtn = 0;
        for (int i = 0; i < bytes; i++) {
            rtn |= read() << (8*i);
        }
        return rtn;
    }
}
