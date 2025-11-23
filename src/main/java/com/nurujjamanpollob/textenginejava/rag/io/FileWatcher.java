package com.nurujjamanpollob.textenginejava.rag.io;

import com.nurujjamanpollob.textenginejava.rag.ProjectOrchestrator;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher implements Runnable {

    private final Path rootDir;
    private final ProjectOrchestrator orchestrator; // Use Orchestrator instead of Collector directly
    private final String projectId;

    public FileWatcher(Path rootDir, ProjectOrchestrator orchestrator, String projectId) {
        this.rootDir = rootDir;
        this.orchestrator = orchestrator;
        this.projectId = projectId;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Recursive registration needs a specific helper library or manual tree walking
            // Standard Java WatchService only watches the registered directory, not sub-directories automatically.
            // For this example, we assume rootDir, but for deep watching, you need to walk the tree and register all.
            rootDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("[Watcher] Active for: " + rootDir);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedPath = (Path) event.context();
                    Path fullPath = rootDir.resolve(changedPath);

                    if(fullPath.toString().contains(".rag_data")) continue; // Ignore our own index files

                    String type = (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) ? "DELETE" : "MODIFY";

                    // Debounce slightly
                    if(!type.equals("DELETE")) Thread.sleep(100);

                    orchestrator.handleFileChange(projectId, fullPath, type);
                }
                key.reset();
            }
        } catch (InterruptedException ie) {
            System.out.println("Watcher stopped for " + projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}