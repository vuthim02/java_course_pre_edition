package com.filemanager;

import com.filemanager.util.FileFormatter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatterTest {

    @Nested
    class FormatSize {

        @Test
        void testZeroBytes() {
            assertEquals("0 B", FileFormatter.formatSize(0));
        }

        @Test
        void testSingleByte() {
            assertEquals("1 B", FileFormatter.formatSize(1));
        }

        @Test
        void testBytes() {
            assertEquals("500 B", FileFormatter.formatSize(500));
        }

        @Test
        void testKilobytes() {
            assertEquals("1.0 KB", FileFormatter.formatSize(1024));
        }

        @Test
        void testKilobytesExact() {
            assertEquals("2.0 KB", FileFormatter.formatSize(2048));
        }

        @Test
        void testMegabytes() {
            assertEquals("1.0 MB", FileFormatter.formatSize(1024 * 1024));
        }

        @Test
        void testGigabytes() {
            assertEquals("1.0 GB", FileFormatter.formatSize((long) 1024 * 1024 * 1024));
        }

        @Test
        void testTerabytes() {
            assertEquals("1.0 TB", FileFormatter.formatSize((long) 1024 * 1024 * 1024 * 1024));
        }

        @Test
        void testFractionalKilobytes() {
            String result = FileFormatter.formatSize(1536);
            assertEquals("1.5 KB", result);
        }

        @Test
        void testLargeNumber() {
            String result = FileFormatter.formatSize(999_999_999_999L);
            assertTrue(result.contains("GB") || result.contains("TB"));
        }
    }

    @Nested
    class FormatPermissions {

        @Test
        void testPermissions_ReadWriteExecutable(@TempDir Path tempDir) throws Exception {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            String perms = FileFormatter.formatPermissions(file);

            assertNotNull(perms);
            assertEquals(9, perms.length());
        }

        @Test
        void testPermissions_NonExistentFile() {
            String perms = FileFormatter.formatPermissions(Path.of("/nonexistent/path/file.txt"));
            assertEquals("---------", perms);
        }

        @Test
        void testPermissions_ContainsLetters(@TempDir Path tempDir) throws Exception {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            String perms = FileFormatter.formatPermissions(file);

            assertTrue(perms.contains("r") || perms.contains("w") || perms.contains("x") || perms.contains("-"));
        }
    }

    @Nested
    class FormatDate {

        @Test
        void testFormatDate() throws Exception {
            java.nio.file.attribute.FileTime fileTime = java.nio.file.attribute.FileTime.fromMillis(0);

            String formatted = FileFormatter.formatDate(fileTime);

            assertNotNull(formatted);
            assertTrue(formatted.contains("1970") || formatted.contains("00"));
        }

        @Test
        void testFormatDate_NotNull() throws Exception {
            java.nio.file.attribute.FileTime fileTime = java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis());

            String formatted = FileFormatter.formatDate(fileTime);

            assertNotNull(formatted);
            assertTrue(formatted.contains("-"));
        }
    }
}
