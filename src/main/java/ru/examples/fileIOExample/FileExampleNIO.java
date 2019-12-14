package ru.examples.fileIOExample;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileExampleNIO {

    public static void main(String[] args) throws IOException {
        StringBuilder data = new StringBuilder();
        RandomAccessFile aFile = new RandomAccessFile("ReadMe.md", "rw");
        FileChannel inChannel = aFile.getChannel();

        char ch;
        ByteBuffer buf = ByteBuffer.allocate(64);
        int bytesRead = inChannel.read(buf);
        while (bytesRead != -1) {

            System.out.println("Read " + bytesRead);
            buf.flip();

            while (buf.hasRemaining()) {
                ch = (char) buf.get();
                System.out.print(ch);
                data.append(ch);
            }
            System.out.print("\n");
            buf.clear();
            bytesRead = inChannel.read(buf);
        }
        aFile.close();
        System.out.println("\n==============================\n" + data.toString());
    }
}
