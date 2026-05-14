package com.filemanager;

import com.filemanager.service.FileService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
    }

    @Nested
    class ListFiles {

        @Test
        void testListFiles_EmptyDirectory() {
            List<String> entries = fileService.listFiles(tempDir);
            assertTrue(entries.isEmpty());
        }

        @Test
        void testListFiles_WithFiles() throws IOException {
            Files.createFile(tempDir.resolve("test.txt"));
            Files.createFile(tempDir.resolve("data.csv"));

            List<String> entries = fileService.listFiles(tempDir);
            assertEquals(2, entries.size());
        }

        @Test
        void testListFiles_WithDirectory() throws IOException {
            Files.createDirectory(tempDir.resolve("subdir"));
            Files.createFile(tempDir.resolve("file.txt"));

            List<String> entries = fileService.listFiles(tempDir);
            assertEquals(2, entries.size());
        }

        @Test
        void testListFiles_ShowsSize() throws IOException {
            Files.writeString(tempDir.resolve("hello.txt"), "Hello World");

            List<String> entries = fileService.listFiles(tempDir);
            assertTrue(entries.get(0).contains("hello.txt"));
        }

        @Test
        void testListFiles_NonExistentDir() {
            List<String> entries = fileService.listFiles(tempDir.resolve("nonexistent"));
            assertTrue(entries.get(0).contains("Error"));
        }
    }

    @Nested
    class CopyFile {

        @Test
        void testCopyRegularFile() throws IOException {
            Path source = Files.writeString(tempDir.resolve("source.txt"), "content");
            Path target = tempDir.resolve("target.txt");

            fileService.copyFile(source, target);

            assertTrue(Files.exists(target));
            assertEquals("content", Files.readString(target));
        }

        @Test
        void testCopyFile_SourceNotExist() {
            Path source = tempDir.resolve("nonexistent.txt");
            Path target = tempDir.resolve("target.txt");

            assertThrows(IOException.class, () -> fileService.copyFile(source, target));
        }

        @Test
        void testCopyDirectory() throws IOException {
            Path sourceDir = tempDir.resolve("srcdir");
            Files.createDirectory(sourceDir);
            Files.writeString(sourceDir.resolve("file1.txt"), "one");
            Files.writeString(sourceDir.resolve("file2.txt"), "two");

            Path targetDir = tempDir.resolve("tgtdir");
            fileService.copyFile(sourceDir, targetDir);

            assertTrue(Files.exists(targetDir.resolve("file1.txt")));
            assertTrue(Files.exists(targetDir.resolve("file2.txt")));
            assertEquals("one", Files.readString(targetDir.resolve("file1.txt")));
        }

        @Test
        void testCopyOverwriteExisting() throws IOException {
            Path source = Files.writeString(tempDir.resolve("source.txt"), "new");
            Path target = Files.writeString(tempDir.resolve("target.txt"), "old");

            fileService.copyFile(source, target);

            assertEquals("new", Files.readString(target));
        }
    }

    @Nested
    class MoveFile {

        @Test
        void testMoveFile() throws IOException {
            Path source = Files.writeString(tempDir.resolve("source.txt"), "movable");
            Path target = tempDir.resolve("target.txt");

            fileService.moveFile(source, target);

            assertFalse(Files.exists(source));
            assertTrue(Files.exists(target));
            assertEquals("movable", Files.readString(target));
        }

        @Test
        void testMoveFile_SourceNotExist() {
            Path source = tempDir.resolve("nonexistent.txt");
            Path target = tempDir.resolve("target.txt");

            assertThrows(IOException.class, () -> fileService.moveFile(source, target));
        }

        @Test
        void testMoveToNewDirectory() throws IOException {
            Path source = Files.writeString(tempDir.resolve("source.txt"), "data");
            Path target = tempDir.resolve("newdir").resolve("target.txt");

            fileService.moveFile(source, target);

            assertTrue(Files.exists(target));
            assertEquals("data", Files.readString(target));
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void testDeleteRegularFile() throws IOException {
            Path file = Files.createFile(tempDir.resolve("delete_me.txt"));

            fileService.deleteFile(file);

            assertFalse(Files.exists(file));
        }

        @Test
        void testDeleteDirectoryRecursively() throws IOException {
            Path dir = tempDir.resolve("del_dir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("nested.txt"));

            fileService.deleteFile(dir);

            assertFalse(Files.exists(dir));
        }

        @Test
        void testDeleteNonExistentFile() {
            assertThrows(IOException.class,
                    () -> fileService.deleteFile(tempDir.resolve("ghost.txt")));
        }
    }

    @Nested
    class SearchFiles {

        @Test
        void testSearchByGlob() throws IOException {
            Files.createFile(tempDir.resolve("readme.txt"));
            Files.createFile(tempDir.resolve("data.csv"));
            Files.createFile(tempDir.resolve("notes.txt"));

            List<String> results = fileService.searchFiles(tempDir, "*.txt");

            assertEquals(2, results.size());
            assertTrue(results.get(0).endsWith("readme.txt") || results.get(1).endsWith("readme.txt"));
            assertTrue(results.get(0).endsWith("notes.txt") || results.get(1).endsWith("notes.txt"));
        }

        @Test
        void testSearch_NoMatches() throws IOException {
            Files.createFile(tempDir.resolve("readme.txt"));

            List<String> results = fileService.searchFiles(tempDir, "*.pdf");

            assertTrue(results.isEmpty());
        }

        @Test
        void testSearch_Recursive() throws IOException {
            Path subdir = tempDir.resolve("sub");
            Files.createDirectory(subdir);
            Files.createFile(subdir.resolve("deep.txt"));

            List<String> results = fileService.searchFiles(tempDir, "*.txt");

            assertEquals(1, results.size());
        }
    }

    @Nested
    class TreeOutput {

        @Test
        void testPrintTree_RootOnly() {
            List<String> tree = fileService.printTree(tempDir, 0);

            assertEquals(1, tree.size());
            assertTrue(tree.get(0).contains(tempDir.getFileName().toString()));
        }

        @Test
        void testPrintTree_WithDepth() throws IOException {
            Files.createFile(tempDir.resolve("file.txt"));

            List<String> tree = fileService.printTree(tempDir, 1);

            assertEquals(2, tree.size());
        }

        @Test
        void testPrintTree_NestedDirectories() throws IOException {
            Path subdir = tempDir.resolve("subdir");
            Files.createDirectory(subdir);
            Files.createFile(subdir.resolve("nested.txt"));

            List<String> tree = fileService.printTree(tempDir, 2);

            assertTrue(tree.size() >= 3);
            assertTrue(tree.stream().anyMatch(l -> l.contains("nested.txt")));
        }
    }

    @Nested
    class FileInfo {

        @Test
        void testFileInfo_RegularFile() throws IOException {
            Path file = Files.writeString(tempDir.resolve("info.txt"), "some data");

            String info = fileService.fileInfo(file);

            assertTrue(info.contains("info.txt"));
            assertTrue(info.contains("File"));
            assertTrue(info.contains("Name:"));
            assertTrue(info.contains("Size:"));
        }

        @Test
        void testFileInfo_Directory() throws IOException {
            String info = fileService.fileInfo(tempDir);

            assertTrue(info.contains("Directory"));
        }

        @Test
        void testFileInfo_NonExistent() throws IOException {
            String info = fileService.fileInfo(tempDir.resolve("ghost.txt"));

            assertTrue(info.contains("does not exist"));
        }
    }

    @Nested
    class FilePreview {

        @Test
        void testPreviewFile() throws IOException {
            Path file = Files.writeString(tempDir.resolve("preview.txt"), "Hello\nWorld");

            String preview = fileService.previewFile(file);

            assertTrue(preview.contains("Hello"));
            assertTrue(preview.contains("World"));
        }

        @Test
        void testPreviewFile_Truncated() throws IOException {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 30; i++) {
                sb.append("Line ").append(i + 1).append("\n");
            }
            Path file = Files.writeString(tempDir.resolve("long.txt"), sb.toString());

            String preview = fileService.previewFile(file);

            assertTrue(preview.contains("truncated"));
        }

        @Test
        void testPreviewDirectory() throws IOException {
            String preview = fileService.previewFile(tempDir);

            assertTrue(preview.contains("Not a regular file"));
        }

        @Test
        void testPreviewNonExistentFile() throws IOException {
            assertThrows(IOException.class,
                    () -> fileService.previewFile(tempDir.resolve("ghost.txt")));
        }
    }
}
