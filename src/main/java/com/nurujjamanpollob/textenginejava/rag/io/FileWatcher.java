package com.nurujjamanpollob.textenginejava.rag.io;

import com.nurujjamanpollob.textenginejava.rag.ProjectOrchestrator;
import com.nurujjamanpollob.textenginejava.rag.utils.RagLogger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileWatcher implements Runnable {

    private final Path rootDir;
    private final ProjectOrchestrator orchestrator;
    private final String projectId;
    private final Map<Path, WatchKey> watchKeys;

    public FileWatcher(Path rootDir, ProjectOrchestrator orchestrator, String projectId) {
        this.rootDir = rootDir;
        this.orchestrator = orchestrator;
        this.projectId = projectId;
        this.watchKeys = new HashMap<>();
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            registerAllDirectories(watchService, rootDir);
            RagLogger.info("Recursive file watcher active for: " + rootDir);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                Path dir = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    Path name = (Path) event.context();
                    Path child = dir.resolve(name);

                    if (child.toString().contains(".rag_data")) continue;

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAllDirectories(watchService, child);
                            }
                        } catch (IOException x) {
                            // ignore
                        }
                    }

                    String type = (kind == StandardWatchEventKinds.ENTRY_DELETE) ? "DELETE" : "MODIFY";
                    // Simple debounce
                    if (!"DELETE".equals(type)) Thread.sleep(50);

                    orchestrator.handleFileChange(projectId, child, type);
                }

                if (!key.reset()) {
                    watchKeys.remove(dir);
                    if (watchKeys.isEmpty()) break;
                }
            }
        } catch (InterruptedException ie) {
            RagLogger.info("Watcher stopped for " + projectId);
        } catch (Exception e) {
            RagLogger.error("Watcher error: " + e.getMessage());
        }
    }

    private void registerAllDirectories(WatchService watchService, Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.getFileName().toString().startsWith(".") && !dir.getFileName().toString().equals(".rag_data")) {
                    registerDirectory(watchService, dir);
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }

    private void registerDirectory(WatchService watchService, Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeys.put(dir, key);
    }
}