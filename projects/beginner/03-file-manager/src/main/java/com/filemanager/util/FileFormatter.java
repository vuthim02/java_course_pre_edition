package com.filemanager.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileFormatter {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB"};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatSize(long bytes) {
        if (bytes == 0) return "0 B";
        int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
        double size = bytes / Math.pow(1024, unitIndex);
        return unitIndex == 0 ? bytes + " B" : String.format("%.1f %s", size, UNITS[unitIndex]);
    }

    public static String formatPermissions(Path path) {
        try {
            if (!Files.isReadable(path) && !Files.isWritable(path) && !Files.isExecutable(path)) {
                return "---------";
            }
            if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
                return PosixFilePermissions.toString(Files.getPosixFilePermissions(path));
            }
        } catch (Exception ignored) {
        }
        StringBuilder sb = new StringBuilder(9);
        sb.append(Files.isReadable(path) ? 'r' : '-');
        sb.append(Files.isWritable(path) ? 'w' : '-');
        sb.append(Files.isExecutable(path) ? 'x' : '-');
        sb.append(Files.isReadable(path) ? 'r' : '-');
        sb.append(Files.isWritable(path) ? 'w' : '-');
        sb.append(Files.isExecutable(path) ? 'x' : '-');
        sb.append(Files.isReadable(path) ? 'r' : '-');
        sb.append(Files.isWritable(path) ? 'w' : '-');
        sb.append(Files.isExecutable(path) ? 'x' : '-');
        return sb.toString();
    }

    public static String formatDate(FileTime fileTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        return ldt.format(DATE_FMT);
    }
}
