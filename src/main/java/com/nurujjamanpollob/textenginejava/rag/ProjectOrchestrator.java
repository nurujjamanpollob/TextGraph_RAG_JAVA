package com.nurujjamanpollob.textenginejava.rag;

import com.google.gson.Gson;
import com.nurujjamanpollob.textenginejava.rag.context.ContextCollector;
import com.nurujjamanpollob.textenginejava.rag.io.FileWatcher;
import com.nurujjamanpollob.textenginejava.rag.model.ProjectMetadata;
import com.nurujjamanpollob.textenginejava.rag.utils.HashUtils;
import javadev.stringcollections.textreplacor.filesquery.DirectoryReader;
import javadev.stringcollections.textreplacor.mimedetector.TextFileDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ProjectOrchestrator {

    private final ContextCollector collector;
    private final Map<String, Path> activeProjects;
    private final Map<String, ProjectMetadata> projectMetadataMap;
    private final Map<String, Thread> watchers;
    private final Gson gson;

    public ProjectOrchestrator(ContextCollector collector) {
        this.collector = collector;
        this.activeProjects = new HashMap<>();
        this.projectMetadataMap = new HashMap<>();
        this.watchers = new HashMap<>();
        this.gson = new Gson();
    }

    /**
     * Checks if the RAG metadata exists for the given project path.
     * @param rootDirPath The root directory of the project.
     * @return true if metadata exists, false otherwise.
     */
    public boolean checkRagMetadataExists(String rootDirPath) {
        Path root = Paths.get(rootDirPath);
        Path metaFile = root.resolve(".rag_data").resolve("metadata.json");
        return Files.exists(metaFile);
    }

    /**
     * Manually creates the RAG metadata structure for a project.
     * @param projectId The project identifier.
     * @param rootDirPath The root directory of the project.
     * @throws Exception If metadata already exists.
     */
    public void createRagMetadata(String projectId, String rootDirPath) throws Exception {
        if (checkRagMetadataExists(rootDirPath)) {
            throw new Exception("RAG metadata already exists for project: " + projectId);
        }

        Path rootPath = Paths.get(rootDirPath);
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Provided path is not a valid directory: " + rootDirPath);
        }

        // Initialize empty metadata
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setLastIndexed(System.currentTimeMillis());
        metadata.setFileHashes(new HashMap<>());

        // Store in memory
        projectMetadataMap.put(projectId, metadata);
        activeProjects.put(projectId, rootPath);

        // Save to disk (Creates .rag_data folder)
        saveMetadata(projectId, rootPath);

        // Initialize and save empty index
        collector.saveIndexToDisk(projectId, rootPath);

        System.out.println("Initialized RAG metadata for project: " + projectId);
    }

    /**
     * Finds the Project ID associated with a specific file or directory path.
     * This is useful for reverse-looking up context from a file path.
     *
     * @param path The path to search for.
     * @return The projectId if found, or null if the path is not part of any active project.
     */
    public String findProjectId(Path path) {
        Path absPath = path.toAbsolutePath();
        String bestMatchId = null;
        int maxLen = -1;

        // Find the project with the longest matching root path (handles nested projects correctly)
        for (Map.Entry<String, Path> entry : activeProjects.entrySet()) {
            Path projectRoot = entry.getValue().toAbsolutePath();
            if (absPath.startsWith(projectRoot)) {
                // We found a parent project, check if it is more specific than previous matches
                int len = projectRoot.toString().length();
                if (len > maxLen) {
                    maxLen = len;
                    bestMatchId = entry.getKey();
                }
            }
        }
        return bestMatchId;
    }

    public void loadAndSyncProject(String projectId, String rootDirPath) {
        Path rootPath = Paths.get(rootDirPath);
        if (!Files.isDirectory(rootPath)) {
            System.out.println("Error: Path is not a directory.");
            return;
        }

        activeProjects.put(projectId, rootPath);

        // 1. Try to load existing Index & Metadata
        collector.loadIndexFromDisk(projectId, rootPath);
        loadMetadata(projectId, rootPath);

        // 2. Perform Smart Sync (Delta check)
        System.out.println("Syncing project [" + projectId + "]...");
        performSmartSync(projectId, rootPath);

        // 3. Start Watcher
        stopWatcher(projectId); // Stop existing if any
        FileWatcher watcher = new FileWatcher(rootPath, this, projectId);
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();
        watchers.put(projectId, watcherThread);
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
            String currentHash = HashUtils.calculateFileHash(filePath);

            currentHashes.put(relativePath, currentHash);

            // Check if file is new or modified
            if (!knownHashes.containsKey(relativePath) || !knownHashes.get(relativePath).equals(currentHash)) {
                try {
                    // System.out.println("Indexing: " + relativePath);
                    String content = Files.readString(filePath);
                    collector.updateFile(projectId, relativePath, content);
                    updated++;
                } catch (IOException e) {
                    System.err.println("Failed to read: " + relativePath);
                }
            } else {
                skipped++;
            }
        }

        // Check for deleted files (Present in Metadata but not in Current Scan)
        for (String oldFile : knownHashes.keySet()) {
            if (!currentHashes.containsKey(oldFile)) {
                System.out.println("Detected deletion: " + oldFile);
                collector.removeFile(projectId, oldFile);
                updated++;
            }
        }

        // Update Metadata
        metadata.setFileHashes(currentHashes);
        metadata.setLastIndexed(System.currentTimeMillis());

        System.out.printf("Sync Complete. Updated/Added: %d, Unchanged: %d%n", updated, skipped);

        // Save everything to disk
        saveState(projectId);
    }

    // Called by FileWatcher
    public void handleFileChange(String projectId, Path filePath, String eventType) {
        Path root = activeProjects.get(projectId);
        if (root == null) return;

        String relativePath = root.relativize(filePath).toString();

        if (eventType.equals("DELETE")) {
            collector.removeFile(projectId, relativePath);
            projectMetadataMap.get(projectId).getFileHashes().remove(relativePath);
        } else {
            // Modify or Create
            if (!isValidCodeFile(filePath)) return;
            try {
                String content = Files.readString(filePath);
                String newHash = HashUtils.calculateFileHash(filePath);

                collector.updateFile(projectId, relativePath, content);
                projectMetadataMap.get(projectId).getFileHashes().put(relativePath, newHash);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Auto-save state on change (or you could throttle this)
        saveState(projectId);
    }

    private void saveState(String projectId) {
        Path root = activeProjects.get(projectId);
        collector.saveIndexToDisk(projectId, root);
        saveMetadata(projectId, root);
    }

    private void loadMetadata(String projectId, Path root) {
        Path metaFile = root.resolve(".rag_data").resolve("metadata.json");
        if (Files.exists(metaFile)) {
            try {
                String json = Files.readString(metaFile);
                ProjectMetadata meta = gson.fromJson(json, ProjectMetadata.class);
                projectMetadataMap.put(projectId, meta);
            } catch (IOException e) {
                System.err.println("Error loading metadata, starting fresh.");
            }
        }
    }

    private void saveMetadata(String projectId, Path root) {
        Path dataDir = root.resolve(".rag_data");
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            String json = gson.toJson(projectMetadataMap.get(projectId));
            Files.writeString(dataDir.resolve("metadata.json"), json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopWatcher(String projectId) {
        if(watchers.containsKey(projectId)) {
            watchers.get(projectId).interrupt();
        }
    }

    public void searchProject(String projectId, String query) {
        if (!activeProjects.containsKey(projectId)) {
            System.out.println("Project not active. Use 'load <id> <path>' first.");
            return;
        }
        Map<String, List<String>> results = collector.searchGrouped(query, projectId);

        if (results.isEmpty()) {
            System.out.println("No relevant code found.");
        } else {
            results.forEach((file, snippets) -> {
                System.out.println("\n📄 FILE: " + file);
                for (String snippet : snippets) {
                    System.out.println("   --- Snippet ---");
                    System.out.println("   " + snippet.replace("\n", "\n   ").trim());
                }
            });
        }
    }

    private boolean isValidCodeFile(Path file) {
        String name = file.toString();
        if (name.contains(".rag_data") || name.contains("/.") || name.contains("\\.")) return false;
        try {
            return TextFileDetector.isTextFile(file);
        } catch (IOException e) {
            return false;
        }
    }
}