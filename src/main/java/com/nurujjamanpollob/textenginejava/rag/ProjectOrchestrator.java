package com.nurujjamanpollob.textenginejava.rag;

import com.google.gson.Gson;
import com.nurujjamanpollob.textenginejava.rag.config.RagConfig;
import com.nurujjamanpollob.textenginejava.rag.context.ContextCollector;
import com.nurujjamanpollob.textenginejava.rag.io.FileWatcher;
import com.nurujjamanpollob.textenginejava.rag.model.ProjectMetadata;
import com.nurujjamanpollob.textenginejava.rag.model.RAGSearchResult;
import com.nurujjamanpollob.textenginejava.rag.utils.HashUtils;
import com.nurujjamanpollob.textenginejava.rag.utils.PathValidator;
import com.nurujjamanpollob.textenginejava.rag.utils.RagLogger;
import com.nurujjamanpollob.textenginejava.rag.exception.RagException;
import javadev.stringcollections.textreplacor.filesquery.DirectoryReader;
import javadev.stringcollections.textreplacor.mimedetector.TextFileDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ProjectOrchestrator {

    private final RagConfig config;
    private final ContextCollector collector;
    private final Map<String, Path> activeProjects;
    private final Map<String, ProjectMetadata> projectMetadataMap;
    private final Map<String, Thread> watchers;
    private final Gson gson;

    public ProjectOrchestrator(ContextCollector collector) {
        this.config = RagConfig.getInstance();
        this.collector = collector;
        this.activeProjects = new HashMap<>();
        this.projectMetadataMap = new HashMap<>();
        this.watchers = new HashMap<>();
        this.gson = new Gson();
    }

    public boolean checkRagMetadataExists(String rootDirPath) {
        try {
            Path root = PathValidator.validatePath(rootDirPath);
            Path metaFile = root.resolve(".rag_data").resolve("metadata.json");
            return Files.exists(metaFile);
        } catch (Exception e) {
            RagLogger.error("Path check error: " + e.getMessage());
            return false;
        }
    }

    public void createRagMetadata(String projectId, String rootDirPath) throws Exception {
        if (checkRagMetadataExists(rootDirPath)) {
            throw new RagException("Metadata already exists for: " + projectId);
        }

        Path rootPath = PathValidator.validatePath(rootDirPath);
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Not a directory: " + rootDirPath);
        }

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setLastIndexed(System.currentTimeMillis());
        metadata.setFileHashes(new HashMap<>());

        projectMetadataMap.put(projectId, metadata);
        activeProjects.put(projectId, rootPath);

        saveMetadata(projectId, rootPath);
        collector.saveIndexToDisk(projectId, rootPath);
        RagLogger.info("Initialized RAG project: " + projectId);
    }

    public String findProjectId(Path path) {
        try {
            Path absPath = path.toAbsolutePath().normalize();
            String bestMatchId = null;
            int maxLen = -1;

            for (Map.Entry<String, Path> entry : activeProjects.entrySet()) {
                Path projectRoot = entry.getValue().toAbsolutePath();
                if (absPath.startsWith(projectRoot)) {
                    int len = projectRoot.toString().length();
                    if (len > maxLen) {
                        maxLen = len;
                        bestMatchId = entry.getKey();
                    }
                }
            }
            return bestMatchId;
        } catch (Exception e) {
            return null;
        }
    }

    public void loadAndSyncProject(String projectId, String rootDirPath) {
        try {
            Path rootPath = PathValidator.validatePath(rootDirPath);
            if (!Files.isDirectory(rootPath)) {
                RagLogger.error("Invalid project directory: " + rootPath);
                return;
            }

            activeProjects.put(projectId, rootPath);

            collector.loadIndexFromDisk(projectId, rootPath);
            loadMetadata(projectId, rootPath);

            RagLogger.info("Syncing project [" + projectId + "]...");
            performSmartSync(projectId, rootPath);

            stopWatcher(projectId);
            FileWatcher watcher = new FileWatcher(rootPath, this, projectId);
            Thread watcherThread = new Thread(watcher);
            watcherThread.start();
            watchers.put(projectId, watcherThread);

        } catch (Exception e) {
            RagLogger.error("Failed to load project: " + e.getMessage());
        }
    }

    private void performSmartSync(String projectId, Path rootPath) {
        ProjectMetadata metadata = projectMetadataMap.computeIfAbsent(projectId, k -> new ProjectMetadata());
        Map<String, String> knownHashes = metadata.getFileHashes();
        Map<String, String> currentHashes = new HashMap<>();

        DirectoryReader directoryReader = new DirectoryReader(rootPath.toString());
        List<File> allFiles = directoryReader.listAllFiles();

        int updated = 0;
        int skipped = 0;

        for (File file : allFiles) {
            Path filePath = file.toPath();
            if (!isValidCodeFile(filePath)) continue;

            String relativePath = rootPath.relativize(filePath).toString();
            // Hash calculation is memory safe
            String currentHash = HashUtils.calculateFileHash(filePath);
            currentHashes.put(relativePath, currentHash);

            if (!knownHashes.containsKey(relativePath) || !knownHashes.get(relativePath).equals(currentHash)) {
                // Use streaming update to handle large files efficiently
                collector.updateFileStreaming(projectId, relativePath, filePath);
                updated++;
            } else {
                skipped++;
            }
        }

        // Handle deletions
        for (String oldFile : knownHashes.keySet()) {
            if (!currentHashes.containsKey(oldFile)) {
                collector.removeFile(projectId, oldFile);
                updated++;
            }
        }

        metadata.setFileHashes(currentHashes);
        metadata.setLastIndexed(System.currentTimeMillis());
        RagLogger.info("Sync Complete. Updated: " + updated + ", Unchanged: " + skipped);
        saveState(projectId);
    }

    public void handleFileChange(String projectId, Path filePath, String eventType) {
        Path root = activeProjects.get(projectId);
        if (root == null) return;

        try {
            Path validatedPath = PathValidator.validateAndNormalizePath(filePath.toString(), root);
            String relativePath = root.relativize(validatedPath).toString();

            if ("DELETE".equals(eventType)) {
                collector.removeFile(projectId, relativePath);
                if (projectMetadataMap.containsKey(projectId)) {
                    projectMetadataMap.get(projectId).getFileHashes().remove(relativePath);
                }
            } else {
                if (!isValidCodeFile(validatedPath)) return;

                // Use streaming to be memory safe
                collector.updateFileStreaming(projectId, relativePath, validatedPath);

                String newHash = HashUtils.calculateFileHash(validatedPath);
                projectMetadataMap.get(projectId).getFileHashes().put(relativePath, newHash);
            }
            saveState(projectId);
        } catch (Exception e) {
            RagLogger.error("Error handling file change: " + e.getMessage());
        }
    }

    private void saveState(String projectId) {
        Path root = activeProjects.get(projectId);
        if (root != null) {
            collector.saveIndexToDisk(projectId, root);
            saveMetadata(projectId, root);
        }
    }

    private void loadMetadata(String projectId, Path root) {
        Path metaFile = root.resolve(".rag_data").resolve("metadata.json");
        if (Files.exists(metaFile)) {
            try {
                String json = Files.readString(metaFile);
                ProjectMetadata meta = gson.fromJson(json, ProjectMetadata.class);
                projectMetadataMap.put(projectId, meta);
            } catch (IOException e) {
                RagLogger.error("Corrupt metadata, starting fresh.");
            }
        }
    }

    private void saveMetadata(String projectId, Path root) {
        try {
            Path dataDir = root.resolve(".rag_data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            String json = gson.toJson(projectMetadataMap.get(projectId));
            Files.writeString(dataDir.resolve("metadata.json"), json);
        } catch (IOException e) {
            RagLogger.error("Failed to save metadata: " + e.getMessage());
        }
    }

    private void stopWatcher(String projectId) {
        if(watchers.containsKey(projectId)) {
            Thread t = watchers.get(projectId);
            if (t != null) t.interrupt();
        }
    }

    /**
     * Searches the loaded project for relevant text segments based on the query.
     * @param projectId The project identifier.
     * @param query The search query.
     */
    public void searchProject(String projectId, String query) {
        if (!activeProjects.containsKey(projectId)) {
            System.out.println("Project not loaded.");
            return;
        }
        Map<String, List<String>> results = collector.searchGrouped(query, projectId, -1, -1);
        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach((file, snippets) -> {
                System.out.println("\n📄 " + file);
                snippets.forEach(s -> System.out.println("   --- " + s.replace("\n", " ").trim() + "..."));
            });
        }
    }

    /**
     * Searches the loaded project for relevant text segments based on the query, and print results.
     * @param projectId The project identifier.
     * @param query The search query.
     * @param maxResults Maximum number of results to return.
     * @param minScore Minimum similarity score threshold.
     */
    public void searchProject(String projectId, String query, int maxResults, double minScore) {
        if (!activeProjects.containsKey(projectId)) {
            System.out.println("Project not loaded.");
            return;
        }
        Map<String, List<String>> results = collector.searchGrouped(query, projectId, maxResults, minScore);
        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach((file, snippets) -> {
                System.out.println("\n📄 " + file);
                snippets.forEach(s -> System.out.println("   --- " + s.replace("\n", " ").trim() + "..."));
            });
        }
    }

    /**
     * Searches the loaded project for relevant text segments based on the query.
     * @param projectId The project identifier.
     * @param query The search query.
     * @param maxResults Maximum number of results to return.
     * @param minScore Minimum similarity score threshold.
     * @return List of {@link com.nurujjamanpollob.textenginejava.rag.model.RAGSearchResult} objects.
     */
    public List<RAGSearchResult> searchProjectAndGetResult(String projectId, String query, int maxResults, double minScore) {
        List<com.nurujjamanpollob.textenginejava.rag.model.RAGSearchResult> searchResults = new ArrayList<>();

        Map<String, List<String>> results = collector.searchGrouped(query, projectId, maxResults, minScore);
        results.forEach((file, snippets) -> {
            double score = 0.0; // Placeholder for actual score calculation
            searchResults.add(new com.nurujjamanpollob.textenginejava.rag.model.RAGSearchResult(snippets, score, file));
        });
        return searchResults;
    }

    /**
     * Simple text file validation to filter out binaries and irrelevant files.
     * @param file The file path to validate.
     * @return True if it's a valid text/code file, false otherwise.
     */
    private boolean isValidCodeFile(Path file) {
        String name = file.toString();
        if (name.contains(".rag_data") || name.contains(File.separator + ".")) return false;
        try {
            return TextFileDetector.isTextFile(file);
        } catch (IOException e) {
            return false;
        }
    }
}