package com.nunes.uploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryList {
    public static void main(String[] args) throws IOException {
        String fileName = System.getProperty("user.home") + "/files";

        List<File> files = new ArrayList<>();

        Files.lines(Paths.get(fileName)).forEach(line -> files.add(new File(line)));

        list(files, "");
    }

    private static void list(List<File> files, String indent) {
        if (files != null) {
            files.forEach(file -> {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        System.out.println(indent + file.getName());
                        if (file.listFiles() != null) {
                            list(Arrays.asList(file.listFiles()), indent + "-");
                        }
                    } else {
                        try {
                            System.out.println(indent + file.getName() + " MIMETYPE: " + Files.probeContentType(file.toPath()));
                        } catch (IOException e) {
                            System.out.println("FAILED to determine mimetype");
                        }
                    }
                } else {
                    System.out.println("FILE DOS NOT EXISTS: " + file.getAbsoluteFile());
                }
            });
        }
    }
}
