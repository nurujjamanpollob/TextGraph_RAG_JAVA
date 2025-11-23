package com.nurujjamanpollob.textenginejava.rag.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectProfile {
    private final String projectName;
    private final String rootPath;
    private int totalFiles;
    private long totalSizeBytes;
    private final Map<String, Integer> languageBreakdown; // e.g., "java" -> 50

    public ProjectProfile(String projectName, String rootPath) {
        this.projectName = projectName;
        this.rootPath = rootPath;
        this.languageBreakdown = new HashMap<>();
        this.totalFiles = 0;
        this.totalSizeBytes = 0;
    }

    public void addFile(String extension, long size) {
        this.totalFiles++;
        this.totalSizeBytes += size;
        this.languageBreakdown.merge(extension, 1, Integer::sum);
    }

    public String getFormattedSize() {
        if (totalSizeBytes < 1024) return totalSizeBytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(totalSizeBytes)) / 10;
        return String.format("%.1f %sB", (double)totalSizeBytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    @Override
    public String toString() {
        String breakdown = languageBreakdown.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by count desc
                .limit(5) // Top 5 languages
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));

        return String.format("Project: %s\nPath: %s\nFiles: %d | Size: %s\nTop Languages: [%s]",
                projectName, rootPath, totalFiles, getFormattedSize(), breakdown);
    }

    // Getters
    public int getTotalFiles() { return totalFiles; }
    public Map<String, Integer> getLanguageBreakdown() { return languageBreakdown; }
}