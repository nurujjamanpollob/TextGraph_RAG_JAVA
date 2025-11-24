package com.nurujjamanpollob.textenginejava.rag;

import com.nurujjamanpollob.textenginejava.rag.context.ContextCollector;
import com.nurujjamanpollob.textenginejava.rag.model.ProjectProfile;
import com.nurujjamanpollob.textenginejava.rag.utils.ProjectInfoExtractor;
import com.nurujjamanpollob.textenginejava.rag.utils.RagLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ContextCollector collector = new ContextCollector();
        ProjectOrchestrator orchestrator = new ProjectOrchestrator(collector);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Intelligent RAG System v1.1 ===");
        System.out.println("Commands: load <path>, search <query>, exit");

        String currentProject = null;

        while (true) {
            String prompt = (currentProject == null) ? "(no-project)" : "[" + currentProject + "]";
            System.out.print(prompt + "> ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "create":
                        if (parts.length < 3) {
                            System.out.println("Usage: create <project_id> <path>");
                        } else {
                            orchestrator.createRagMetadata(parts[1], parts[2]);
                        }
                        break;

                    case "load":
                        if (parts.length < 2) {
                            System.out.println("Usage: load <path>");
                            break;
                        }
                        Path pathObj = Paths.get(parts[1]).toAbsolutePath().normalize();
                        if (!Files.isDirectory(pathObj)) {
                            System.out.println("Invalid directory.");
                            break;
                        }

                        String pId = pathObj.getFileName().toString();
                        if (!orchestrator.checkRagMetadataExists(pathObj.toString())) {
                            System.out.println("Creating metadata for " + pId);
                            orchestrator.createRagMetadata(pId, pathObj.toString());
                        }

                        // Analysis
                        ProjectProfile profile = ProjectInfoExtractor.analyzeProject(pId, pathObj);
                        System.out.println(profile);

                        // Indexing
                        orchestrator.loadAndSyncProject(pId, pathObj.toString());
                        currentProject = pId;
                        System.out.println("Loaded project: " + pId);
                        break;

                    case "search":
                        if (currentProject == null) {
                            System.out.println("Load a project first.");
                            break;
                        }
                        if (parts.length < 2) {
                            System.out.println("Enter a query.");
                            break;
                        }
                        StringBuilder query = new StringBuilder();
                        for(int i=1; i<parts.length; i++) query.append(parts[i]).append(" ");
                        orchestrator.searchProject(currentProject, query.toString().trim());
                        break;


                    case "exit":
                        RagLogger.info("Shutting down.");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                RagLogger.error("Command error: " + e.getMessage());
            }
        }
    }
}