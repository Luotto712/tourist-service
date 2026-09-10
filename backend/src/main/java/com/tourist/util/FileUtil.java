package com.tourist.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FileUtil {

    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx",
            "mp4", "avi", "mov",
            "jpg", "jpeg", "png", "gif"
    ));

    /**
     * Check if the file extension is in the allowed set.
     */
    public static boolean isValidExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String ext = getFileExtension(filename);
        return ext != null && ALLOWED_EXTENSIONS.contains(ext.toLowerCase());
    }

    /**
     * Extract the file extension from a filename.
     * Returns null if no extension is found.
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex + 1);
    }

    /**
     * Generate a relative file path in the format: {relatedType}/{yyyy}/{MM}/{uuid}.{ext}
     */
    public static String generateFilePath(String relatedType, String extension) {
        if (relatedType == null) {
            relatedType = "common";
        }
        LocalDate now = LocalDate.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = (extension != null && !extension.isEmpty()) ? extension : "dat";

        return relatedType + "/" + year + "/" + month + "/" + uuid + "." + ext;
    }

}
