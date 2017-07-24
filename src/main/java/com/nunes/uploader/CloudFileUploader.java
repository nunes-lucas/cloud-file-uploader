package com.nunes.uploader;

import com.nunes.uploader.repository.Repository;
import com.nunes.uploader.repository.gdrive.GDriveRepositoryImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CloudFileUploader {
    private static final Logger logger = LoggerFactory.getLogger(CloudFileUploader.class);

    private static final String BACKUP_FOLDER_NAME = "Backup_" + System.getProperty("user.name");
    private static Repository driveService;

    public static void main(String[] args) throws IOException {
        logger.info("Started files upload");

        driveService = new GDriveRepositoryImpl();

        List<java.io.File> files = readFilesToBackup();

        String rootFolderId = driveService.findOrCreateFolder(BACKUP_FOLDER_NAME);

        uploadFiles(rootFolderId, files);

        logger.info("End files upload");
    }

    /**
     * Read backup_files file at user's home folder
     * @return List of files
     * @throws IOException
     */
    private static List<File> readFilesToBackup() throws IOException {
        String fileName = System.getProperty("user.home") + "/backup_files";

        return Files.lines(Paths.get(fileName)).map(File::new).collect(Collectors.toList());
    }

    /**
     * Upload files and directories to cloud provider
     * @param folderID root folder id
     * @param files    files list
     */
    private static void uploadFiles(String folderID, List<java.io.File> files) {
        files.forEach(file -> {
            if (file.exists()) {
                if (file.isDirectory()) {
                    try {
                        String newFolderID = driveService.createFolder(folderID, file.getName());

                        File[] children = file.listFiles();

                        if (children != null) {
                            uploadFiles(newFolderID, Arrays.asList(children));
                        }
                    } catch (IOException e) {
                        logger.error("Error creating folder: {}", e, file.getAbsoluteFile());
                    }
                } else if (file.isFile()) {
                    try {
                        driveService.createOrUploadFile(folderID, file);
                    } catch (IOException e) {
                        logger.error("Error uploading file: {}", e, file.getAbsoluteFile());
                    }
                }
            } else {
                logger.error("File does not exists: {}", file.getAbsoluteFile());
            }
        });
    }
}