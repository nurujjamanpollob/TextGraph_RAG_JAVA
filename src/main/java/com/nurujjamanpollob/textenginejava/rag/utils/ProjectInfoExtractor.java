package com.nurujjamanpollob.textenginejava.rag.utils;

import com.nurujjamanpollob.textenginejava.rag.model.ProjectProfile;
import javadev.stringcollections.textreplacor.mimedetector.TextFileDetector; // Assuming this exists from your previous code

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public class ProjectInfoExtractor {

    // Common binary or irrelevant extensions to skip during summary
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            "class", "jar", "exe", "dll", "so", "png", "jpg", "jpeg", "gif", "ico", "zip", "tar", "gz"
    );

    /**
     * Scans the directory and returns a statistical profile.
     * @param projectId The ID to assign to the profile.
     * @param rootDir The root directory path.
     * @return A populated ProjectProfile.
     */
    public static ProjectProfile analyzeProject(String projectId, Path rootDir) {
        ProjectProfile profile = new ProjectProfile(projectId, rootDir.toAbsolutePath().toString());

        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    // Skip hidden directories and the internal RAG index folder
                    String dirName = dir.getFileName().toString();
                    if (dirName.startsWith(".") || dirName.equals(".rag_data") || dirName.equals("target") || dirName.equals("build")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isValidSourceFile(file)) {
                        String ext = getExtension(file.getFileName().toString());
                        profile.addFile(ext, attrs.size());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Silently skip unreadable files
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error scanning project structure: " + e.getMessage());
        }

        return profile;
    }

    private static boolean isValidSourceFile(Path file) {
        String name = file.getFileName().toString();

        // 1. Basic Name Check
        if (name.startsWith(".")) return false;

        // 2. Extension Check (Fastest)
        String ext = getExtension(name);
        if (IGNORED_EXTENSIONS.contains(ext)) return false;

        // 3. Optional: Use your existing TextFileDetector for deeper check
        // If scanning is too slow, remove this try-catch block and rely on extension only.
        try {
            return TextFileDetector.isTextFile(file);
        } catch (IOException e) {
            return false;
        }
    }

    private static String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1).toLowerCase();
        }
        return "unknown"; // Makefiles, Dockerfiles, etc.
    }
}