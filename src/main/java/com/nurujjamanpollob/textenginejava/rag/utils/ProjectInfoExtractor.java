package com.nurujjamanpollob.textenginejava.rag.utils;

import com.nurujjamanpollob.textenginejava.rag.model.ProjectProfile;
import javadev.stringcollections.textreplacor.mimedetector.TextFileDetector; // Assuming this exists from your previous code

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public class ProjectInfoExtractor {


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

        // skip file from .rag_data directory
        if (file.toString().contains(".rag_data")) {
            return false;
        }

        // Only Text files can be considered source files
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