package com.nunes.uploader.repository.gdrive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.nunes.uploader.repository.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GDriveRepositoryImpl extends GDrive implements Repository {
    private static final Logger logger = LoggerFactory.getLogger(GDriveRepositoryImpl.class);

    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private Drive driveService;

    public GDriveRepositoryImpl() throws IOException {
        driveService = getDriveService();
    }

    @Override
    public String findFolder(String folderName) throws IOException {
        String pageToken = null;
        String folderId = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("mimeType='" + FOLDER_MIME_TYPE + "' and name = '" + folderName + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(*)")
                    .setPageToken(pageToken)
                    .execute();

            if (!result.getFiles().isEmpty()) {
                folderId = result.getFiles().get(0).getId();
                break;
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return folderId;
    }

    @Override
    public String findOrCreateFolder(String folderName) throws IOException {
        String folderId = findFolder(folderName);

        if (folderId == null) {
            folderId = createFolder(folderName);
        }

        return folderId;
    }

    @Override
    public String createFolder(String folderName) throws IOException {
        return createFolder(null, folderName);
    }

    @Override
    public String createFolder(String parentFolderId, String folderName) throws IOException {
        Map<String, File> folders = listFolders(parentFolderId);

        if (folders.containsKey(folderName)) {
            return folders.get(folderName).getId();
        }

        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        if (parentFolderId != null) {
            folderMetadata.setParents(Collections.singletonList(parentFolderId));
        }
        folderMetadata.setMimeType(FOLDER_MIME_TYPE);

        File folder = driveService.files().create(folderMetadata)
                .setFields("id,name")
                .execute();

        logger.info("Created folder [{}] - {}", folder.getId(), folder.getName());

        return folder.getId();
    }

    @Override
    public void createFile(String parentFolderId, java.io.File file) throws IOException {
        String fileName = file.getName();
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));

        FileContent mediaContent = new FileContent(Files.probeContentType(file.toPath()), file);

        File newFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, parents").execute();
        logger.info("Uploaded file [{}] - {}, to {}", newFile.getId(), file.getName(), parentFolderId);
    }

    @Override
    public void updateFileContent(String fileId, java.io.File file) throws IOException {
        FileContent mediaContent = new FileContent(Files.probeContentType(file.toPath()), file);

        File updatedFile = driveService.files().update(fileId, new File(), mediaContent).execute();

        logger.info("Updated file [{}] - {}", updatedFile.getId(), file.getName());
    }

    @Override
    public void createOrUploadFile(String parentFolderId, java.io.File file) throws IOException {
        String fileName = file.getName();

        Map<String, File> files = listFiles(parentFolderId);

        if (files.containsKey(fileName)) {
            updateFileContent(files.get(fileName).getId(), file);
        } else {
            createFile(parentFolderId, file);
        }
    }

    private Map<String, File> listFiles(String parentFolderId) throws IOException {
        if (parentFolderId == null) {
            return Collections.emptyMap();
        }
        String query = "mimeType!='" + FOLDER_MIME_TYPE + "'";
        return listFilesByQuery(query, parentFolderId);
    }

    private Map<String, File> listFolders(String parentFolderId) throws IOException {
        if (parentFolderId == null) {
            return Collections.emptyMap();
        }
        String query = "mimeType='" + FOLDER_MIME_TYPE + "'";
        return listFilesByQuery(query, parentFolderId);
    }

    private Map<String, File> listFilesByQuery(String query, String parentFolderId) throws IOException {
        String pageToken = null;
        Map<String, File> filesMap = new HashMap<>();

        do {
            FileList result = driveService.files().list()
                    .setQ(query + " and '" + parentFolderId + "' in parents and trashed = false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(*)")
                    .setPageToken(pageToken)
                    .execute();

            filesMap.putAll(result.getFiles().stream().collect(Collectors.toMap(File::getName,
                    Function.identity())));

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return filesMap;
    }
}
