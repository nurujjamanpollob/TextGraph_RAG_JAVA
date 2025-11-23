package com.nurujjamanpollob.textenginejava.rag;

import com.nurujjamanpollob.textenginejava.rag.context.ContextCollector;
import com.nurujjamanpollob.textenginejava.rag.model.ProjectProfile;
import com.nurujjamanpollob.textenginejava.rag.utils.ProjectInfoExtractor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ContextCollector collector = new ContextCollector();
        ProjectOrchestrator orchestrator = new ProjectOrchestrator(collector);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Intelligent RAG System ===");
        System.out.println("Commands:");
        System.out.println("  load <path>                -> Load project from path (Auto-ID)");
        System.out.println("  load <project_id> <path>   -> Load/Index a project with specific ID");
        System.out.println("  create <project_id> <path> -> Create RAG metadata manually");
        System.out.println("  use <project_id>           -> Switch active context");
        System.out.println("  refresh                    -> Force re-scan of current project");
        System.out.println("  search <query>             -> Search code");
        System.out.println("  exit");

        String currentProject = null;

        while (true) {
            String prompt = (currentProject == null) ? "(no-project)" : "[" + currentProject + "]";
            System.out.print(prompt + "> ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            // Split allowing for quoted paths could be added here, but staying simple for now
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "create":
                        if (parts.length < 3) {
                            System.out.println("Usage: create <project_id> <path>");
                        } else {
                            try {
                                orchestrator.createRagMetadata(parts[1], parts[2]);
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        break;

                    case "load":
                        if (parts.length < 2) {
                            System.out.println("Usage: load <path> OR load <project_id> <path>");
                            break;
                        }

                        String pId;
                        String pPath;
                        Path pathObj;

                        // Logic to handle "load <path>" vs "load <id> <path>"
                        if (parts.length == 2) {
                            // Case: load <path>
                            pPath = parts[1];
                            pathObj = Paths.get(pPath).toAbsolutePath().normalize();

                            // 1. Check if this path is already loaded
                            String existingId = orchestrator.findProjectId(pathObj);
                            if (existingId != null) {
                                System.out.println("Path is already loaded as project: " + existingId);
                                currentProject = existingId;
                                break;
                            }

                            // 2. Not loaded, infer ID from folder name
                            pId = pathObj.getFileName().toString();
                            System.out.println("Inferred Project ID: " + pId);

                        } else {
                            // Case: load <id> <path>
                            pId = parts[1];
                            pPath = parts[2];
                            pathObj = Paths.get(pPath).toAbsolutePath().normalize();
                        }

                        // Validate Directory
                        if (!Files.isDirectory(pathObj)) {
                            System.out.println("Error: Path is not a valid directory: " + pathObj);
                            break;
                        }

                        // Check and Auto-Create Metadata if missing
                        if (!orchestrator.checkRagMetadataExists(pathObj.toString())) {
                            System.out.println("RAG Metadata missing. Creating automatically for ID: " + pId);
                            try {
                                orchestrator.createRagMetadata(pId, pathObj.toString());
                            } catch (Exception e) {
                                System.out.println("Failed to create metadata: " + e.getMessage());
                                break;
                            }
                        }

                        // 1. Fast Analysis
                        System.out.println("Analyzing project structure...");
                        ProjectProfile profile = ProjectInfoExtractor.analyzeProject(pId, pathObj);
                        System.out.println(profile);

                        System.out.println("------------------------------------------------");

                        // 2. Actual Heavy Indexing
                        orchestrator.loadAndSyncProject(pId, pathObj.toString());

                        // Set active project and confirm
                        currentProject = pId;
                        System.out.println("Successfully loaded. Active Project ID: " + currentProject);
                        break;

                    case "use":
                        if (parts.length < 2) {
                            System.out.println("Usage: use <project_id>");
                        } else {
                            // Verify if ID exists? (Optional, but good UX)
                            // Since Orchestrator doesn't expose a "hasProject" check easily without map access,
                            // we just set it. If search fails, it fails.
                            currentProject = parts[1];
                            System.out.println("Switched to project: " + currentProject);
                        }
                        break;

                    case "refresh":
                        if (currentProject == null) {
                            System.out.println("No active project to refresh.");
                        } else {
                            System.out.println("To refresh, please reload the project using 'load'.");
                            // Ideally, we'd lookup the path for currentProject and call loadAndSyncProject again.
                        }
                        break;

                    case "search":
                        if (currentProject == null) {
                            System.out.println("Select a project first using 'load' or 'use'.");
                        } else {
                            // Reconstruct query from remaining parts
                            StringBuilder queryBuilder = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) {
                                queryBuilder.append(parts[i]).append(" ");
                            }
                            String query = queryBuilder.toString().trim();

                            if (query.isEmpty()) {
                                System.out.println("Please provide a search query.");
                            } else {
                                orchestrator.searchProject(currentProject, query);
                            }
                        }
                        break;

                    case "exit":
                        System.out.println("Goodbye.");
                        System.exit(0);

                    default:
                        System.out.println("Unknown command: " + command);
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}