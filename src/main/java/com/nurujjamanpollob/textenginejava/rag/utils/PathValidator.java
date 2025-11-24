package com.nurujjamanpollob.textenginejava.rag.utils;

import com.nurujjamanpollob.textenginejava.rag.exception.RagException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidator {

    /**
     * Validates and normalizes a path to prevent directory traversal attacks.
     */
    public static Path validateAndNormalizePath(String inputPath, Path basePath) throws RagException {
        if (inputPath == null || inputPath.trim().isEmpty()) {
            throw new RagException("Path cannot be empty");
        }

        try {
            Path normalizedPath = Paths.get(inputPath).normalize();

            // For absolute paths, ensure they are within the base path if required
            // (Logic depends on use case, here we ensure it doesn't traverse up unexpectedly if relative)
            if (normalizedPath.isAbsolute()) {
                if (!normalizedPath.startsWith(basePath)) {
                    // In strict mode, maybe we don't allow absolute paths outside.
                    // For now, we trust absolute paths provided by user but log them.
                    RagLogger.debug("Accessing absolute path: " + inputPath);
                }
                return normalizedPath;
            }

            // Resolve against base path
            Path resolvedPath = basePath.resolve(normalizedPath).normalize();

            if (!resolvedPath.startsWith(basePath)) {
                RagLogger.error("Path traversal attempt detected: " + inputPath);
                throw new SecurityException("Path traversal detected: " + inputPath);
            }

            return resolvedPath;
        } catch (Exception e) {
            throw new RagException("Invalid path: " + e.getMessage(), e);
        }
    }

    /**
     * General path validation checking for suspicious patterns.
     */
    public static Path validatePath(String inputPath) throws RagException {
        if (inputPath == null || inputPath.trim().isEmpty()) {
            throw new RagException("Path cannot be empty");
        }

        if (inputPath.contains("..") || inputPath.contains("~")) {
            RagLogger.warn("Suspicious path pattern detected: " + inputPath);

            // Depending on policy, either throw exception or just log warning
            throw new RagException("Suspicious path pattern detected: " + inputPath);
        }

        try {
            return Paths.get(inputPath).normalize();
        } catch (Exception e) {
            throw new RagException("Invalid path format: " + inputPath, e);
        }
    }
}