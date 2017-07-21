package com.nunes.uploader.repository;

import java.io.IOException;

public interface Repository {
    /**
     * Find folder by name
     * @param folderName folder name
     * @return folder id
     * @throws IOException
     */
    String findFolder(String folderName) throws IOException;

    /**
     * Find or create folder name
     * @param folderName folder name
     * @return folder id
     * @throws IOException
     */
    String findOrCreateFolder(String folderName) throws IOException;

    /**
     * Create folder by name on root directory
     * @param folderName folder name
     * @return folder id
     * @throws IOException
     */
    String createFolder(String folderName) throws IOException;

    /**
     * Create folder by name on an specific parent folder
     * @param parentFolderId prent folder id
     * @param folderName     folder name
     * @return folder id
     * @throws IOException
     */
    String createFolder(String parentFolderId, String folderName) throws IOException;

    /**
     * Create file on an specific parent folder
     * @param parentFolderId prent folder id
     * @param file           file content
     * @throws IOException
     */
    void createFile(String parentFolderId, java.io.File file) throws IOException;

    /**
     * Update a file content
     * @param fileId file id
     * @param file   file content
     * @throws IOException
     */
    void updateFileContent(String fileId, java.io.File file) throws IOException;

    /**
     * Create or update file on an specific parent folder
     * @param parentFolderId parent folder id
     * @param file           file content
     * @throws IOException
     */
    void createOrUploadFile(String parentFolderId, java.io.File file) throws IOException;
}
