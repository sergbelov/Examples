package ru.examples.fileIOExample.fileIterator;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Created by Сергей on 30.04.2018.
 */
public class FileIterator implements Iterator<String>{
    private Queue<File> files = new LinkedList<>();

    public FileIterator(String path){
//        files.add(new File(root + path));
        files.add(new File(path));
    }
    @Override
    public boolean hasNext() {
        return !files.isEmpty();
    }

    @Override
    public String next() {
        File file = files.peek();
        if (file.isDirectory()){
            for (File subFile : file.listFiles()){
                files.add(subFile);
                System.out.println(subFile);
            }
        }
        return files.poll().getAbsolutePath();
    }

    @Override
    public void remove() {
    }
}
