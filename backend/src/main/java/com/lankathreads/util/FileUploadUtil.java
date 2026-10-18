package com.lankathreads.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility helper for uploading and saving image files locally.
 */
public class FileUploadUtil {

    /**
     * Resolves the base working directory for saving uploaded files.
     */
    public static String getWebDir() {
        return System.getProperty("user.dir");
    }

    /**
     * Saves an uploaded MultipartFile into the specified target directory.
     * 
     * @param uploadDir Target directory relative to project root (e.g. "uploads/profile-photos/1")
     * @param fileName Name to save the file as
     * @param multipartFile The uploaded file object
     */
    public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(getWebDir(), uploadDir);

        // Create the directory if it doesn't already exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Copy the uploaded file stream into the target path
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save image file: " + fileName, e);
        }
    }
}
