package sample;

import javafx.beans.property.SimpleStringProperty;

import javax.sound.sampled.AudioFormat;
import java.io.EOFException;
import java.io.IOException;

/**
 * Created by isaac on 3/29/17.
 */
public class LocalSong extends Song {
    private String path;
    public RandomAccessFile2 file;
    private static byte[] buffer = new byte[60*1024];
    private int fmtChunkSize;
    private int channels;
    private int sampleRate;
    private int byteRate;
    private int blockAlign;
    private int bitsPerSample;
    private int size;

    public LocalSong(String path, String filename, String name, String singer, String album, int size) throws Exception {
        super.name = new SimpleStringProperty(name);
        super.singer = new SimpleStringProperty(singer);
        super.album = new SimpleStringProperty(album);
        super.filename = filename;
        this.path = path;
        file = new RandomAccessFile2(path, "r");
        if (file.read() != 'R') {
            throw new Exception("The target file is not a RIFF file.");
        }
        file.skipBytes(11);
        if (file.read() != 'f') {
            throw new Exception("The implementation of this program assumes that fmt is placed before data.");
        }
        file.skipBytes(3);
        fmtChunkSize = file.readLittleEndian(4);
        file.skipBytes(2);
        channels = file.readLittleEndian(2);
        sampleRate = file.readLittleEndian(4);
        byteRate = file.readLittleEndian(4);
        blockAlign = file.readLittleEndian(2);
        bitsPerSample = file.readLittleEndian(2);
        if (file.read() != 'd') {
            throw new Exception("apparently we are not reading the 'data' chunk.");
        }
        file.skipBytes(7);
    }

    @Override
    public byte[] read() throws IOException, InterruptedException {
        if (file.read(buffer) == -1) {
            throw new EOFException();
        }
        return buffer;
    }


    @Override
    public void close() throws IOException {
        file.close();
    }

    @Override
    public AudioFormat getFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample, channels, 4, byteRate/4, false);
    }

    public String serialize() {
        final String separator = ",,";
        final long fileSize = (new java.io.File(path)).length();
        String str = filename + separator + fileSize + separator + name.get() + separator + singer.get() + separator + album.get();
        return str;
    }
}
