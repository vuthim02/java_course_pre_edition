package com.filemanager.service;

import com.filemanager.util.FileFormatter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileService {

    public List<String> listFiles(Path dir) {
        List<String> entries = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.sorted().forEach(p -> {
                try {
                    String type = Files.isDirectory(p) ? "[DIR]" : "[FILE]";
                    long size = Files.size(p);
                    entries.add(String.format("%s %-8s %s", type, FileFormatter.formatSize(size), p.getFileName()));
                } catch (IOException ignored) {
                    entries.add("[????] " + p.getFileName());
                }
            });
        } catch (IOException e) {
            entries.add("Error reading directory: " + e.getMessage());
        }
        return entries;
    }

    public void copyFile(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new NoSuchFileException(source.toString(), null, "Source file does not exist");
        }
        if (Files.isDirectory(source)) {
            copyDirectory(source, target);
        } else {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path dest = target.resolve(source.relativize(dir));
                Files.createDirectories(dest);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path dest = target.resolve(source.relativize(file));
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void moveFile(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new NoSuchFileException(source.toString(), null, "Source file does not exist");
        }
        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException(path.toString(), null, "File does not exist");
        }
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to delete: " + p, e);
                        }
                    });
            }
        } else {
            Files.delete(path);
        }
    }

    public List<String> searchFiles(Path root, String pattern) throws IOException {
        List<String> results = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (matcher.matches(file.getFileName())) {
                    results.add(file.toAbsolutePath().normalize().toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return results;
    }

    public List<String> printTree(Path root, int depth) {
        List<String> lines = new ArrayList<>();
        lines.add(root.toAbsolutePath().normalize().toString());
        if (Files.isDirectory(root) && depth > 0) {
            buildTree(root, "", depth, lines);
        }
        return lines;
    }

    private void buildTree(Path dir, String prefix, int maxDepth, List<String> lines) {
        try (Stream<Path> children = Files.list(dir)) {
            List<Path> sorted = children.sorted().toList();
            for (int i = 0; i < sorted.size(); i++) {
                Path child = sorted.get(i);
                boolean last = i == sorted.size() - 1;
                String connector = last ? "└── " : "├── ";
                lines.add(prefix + connector + child.getFileName());
                int remaining = maxDepth - 1;
                if (Files.isDirectory(child) && remaining > 0) {
                    String newPrefix = prefix + (last ? "    " : "│   ");
                    buildTree(child, newPrefix, remaining, lines);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public String previewFile(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return "Not a regular file";
        }
        String content = Files.readString(path);
        String[] lines = content.split("\n", 21);
        StringBuilder sb = new StringBuilder();
        int count = Math.min(lines.length, 20);
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%4d: %s%n", i + 1, lines[i]));
        }
        if (lines.length > 20) {
            sb.append("... (truncated, file has more lines)");
        }
        return sb.toString();
    }

    public String fileInfo(Path path) throws IOException {
        if (!Files.exists(path)) {
            return "File does not exist: " + path;
        }
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return String.format("""
                Name: %s
                Type: %s
                Size: %s
                Created: %s
                Modified: %s
                Permissions: %s
                Owner: %s
                """,
            path.getFileName(),
            Files.isDirectory(path) ? "Directory" : "File",
            FileFormatter.formatSize(attrs.size()),
            FileFormatter.formatDate(attrs.creationTime()),
            FileFormatter.formatDate(attrs.lastModifiedTime()),
            FileFormatter.formatPermissions(path),
            Files.getOwner(path).getName());
    }
}
