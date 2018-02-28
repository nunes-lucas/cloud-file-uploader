package com.nunes.uploader;

import com.google.common.base.Strings;
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

    //Folder where files will be uploaded in GDrive
    private static final String DEFAULT_BACKUP_FOLDER_NAME = "Backup_" + System.getProperty("user.name");
    //File in user home directory containing the list of folders and files to be uploaded
    private static final String DEFAULT_BACKUP_FILE_NAME = System.getProperty("user.home") + "/backup_files";
    private static Repository driveService;
    private static String folderName = DEFAULT_BACKUP_FOLDER_NAME;
    private static String fileName = DEFAULT_BACKUP_FILE_NAME;

    public static void main(String[] args) throws IOException {
        logger.info("Started files upload");

        readOptions(args);

        driveService = new GDriveRepositoryImpl();

        List<java.io.File> files = readFilesToBackup(fileName);

        String rootFolderId = driveService.findOrCreateFolder(folderName);

        uploadFiles(rootFolderId, files);

        logger.info("End files upload");
    }

    private static void readOptions(String[] args) {
        try {
            if (!Strings.isNullOrEmpty(args[0])) {
                folderName = args[0];
                logger.info("Using folder name: [{}]", folderName);
            }

            if (!Strings.isNullOrEmpty(args[1])) {
                fileName = args[1];
                logger.info("Using file name: [{}]", fileName);
            }
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Missing folder of file, using default values.");
        }
    }

    /**
     * Read backup_files file at user's home folder
     * @return List of files
     * @throws IOException
     */
    private static List<File> readFilesToBackup(String fileName) throws IOException {
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