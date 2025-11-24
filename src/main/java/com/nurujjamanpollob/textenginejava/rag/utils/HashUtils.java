package com.nurujjamanpollob.textenginejava.rag.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String calculateFileHash(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest.digest()) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (IOException e) {
            // Log the actual error internally but don't expose file paths in exceptions
            RagLogger.error("Failed to calculate hash for file: " + e.getMessage());
            throw new RuntimeException("Failed to calculate file hash");
        } catch (NoSuchAlgorithmException e) {
            RagLogger.error("Hashing algorithm not available: " + e.getMessage());
            throw new RuntimeException("Hashing algorithm not available");
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            RagLogger.error("Unexpected error during hash calculation: " + e.getMessage());
            throw new RuntimeException("Failed to calculate file hash");
        }
    }
}