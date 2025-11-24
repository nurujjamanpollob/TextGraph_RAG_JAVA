package com.nurujjamanpollob.textenginejava.rag.context;

import com.google.gson.Gson;
import com.nurujjamanpollob.textenginejava.rag.config.RagConfig;
import com.nurujjamanpollob.textenginejava.rag.utils.RagLogger;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextCollector {

    private final RagConfig config;
    private final EmbeddingModel embeddingModel;
    // We map ProjectID -> Specific Embedding Store to keep them isolated and easily serializable
    private final Map<String, InMemoryEmbeddingStore<TextSegment>> projectStores;
    private final Gson gson;

    public ContextCollector() {
        this.config = RagConfig.getInstance();
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.projectStores = new HashMap<>();
        this.gson = new Gson();
    }

    private InMemoryEmbeddingStore<TextSegment> getStore(String projectId) {
        return projectStores.computeIfAbsent(projectId, k -> new InMemoryEmbeddingStore<>());
    }

    public void updateFile(String projectId, String relativePath, String content) {
        if (content == null || content.trim().isEmpty()) return;

        InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);

        // Remove old chunks for this file
        removeFile(projectId, relativePath);

        Metadata metadata = new Metadata();
        metadata.put("file_path", relativePath);
        metadata.put("project_id", projectId);

        Document doc = Document.from(content, metadata);

        // Optimized splitter for code using configuration values
        List<TextSegment> segments = DocumentSplitters.recursive(config.getChunkSize(), config.getOverlapSize()).split(doc);

        if (!segments.isEmpty()) {
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            store.addAll(embeddings, segments);
        }
    }

    /**
     * Updates a file using streaming for large files to avoid memory issues
     */
    public void updateFileStreaming(String projectId, String relativePath, Path filePath) {
        try {
            // For small files, use the existing method
            if (Files.size(filePath) <= config.getMaxFileSize()) {
                String content = Files.readString(filePath);
                updateFile(projectId, relativePath, content);
                return;
            }

            // For large files, process in chunks
            InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);

            // Remove old chunks for this file
            removeFile(projectId, relativePath);

            processLargeFileInChunks(projectId, relativePath, filePath, store);

        } catch (IOException e) {
            RagLogger.error("Error processing file: " + relativePath + " - " + e.getMessage());
        }
    }

    /**
     * Process large files in chunks to avoid memory issues
     */
    private void processLargeFileInChunks(String projectId, String relativePath, Path filePath,
                                          InMemoryEmbeddingStore<TextSegment> store) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            StringBuilder chunk = new StringBuilder();
            int lineCount = 0;
            int chunkNumber = 0;

            while ((line = reader.readLine()) != null) {
                chunk.append(line).append("\n");
                lineCount++;

                // Process chunk when it reaches the desired size
                if (lineCount >= 100) { // Process every 100 lines as a chunk
                    processChunk(projectId, relativePath, chunk.toString(), chunkNumber++, store);
                    chunk.setLength(0); // Clear the chunk
                    lineCount = 0;
                }
            }

            // Process remaining content
            if (chunk.length() > 0) {
                processChunk(projectId, relativePath, chunk.toString(), chunkNumber, store);
            }
        } catch (IOException e) {
            RagLogger.error("Error reading file: " + relativePath + " - " + e.getMessage());
        }
    }

    /**
     * Process a single chunk of a large file
     */
    private void processChunk(String projectId, String relativePath, String content, int chunkNumber,
                              InMemoryEmbeddingStore<TextSegment> store) {
        if (content == null || content.trim().isEmpty()) return;

        Metadata metadata = new Metadata();
        metadata.put("file_path", relativePath);
        metadata.put("project_id", projectId);
        metadata.put("chunk_number", String.valueOf(chunkNumber));

        Document doc = Document.from(content, metadata);

        // Split the chunk further if needed using configuration values
        List<TextSegment> segments = DocumentSplitters.recursive(config.getChunkSize(), config.getOverlapSize()).split(doc);

        if (!segments.isEmpty()) {
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            store.addAll(embeddings, segments);
        }
    }

    public void removeFile(String projectId, String relativePath) {
        // In MemoryStore, removal by metadata filter is complex.
        // Simpler approach for In-Memory: Filter out matches during search
        // OR (Better for "Refactor"): Rebuild index logic.
        // However, LangChain4j InMemoryStore removeAll(Filter) is available in newer versions.
        // Assuming standard InMemoryStore:

        InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);
        // Note: Current LangChain4j InMemoryStore might not support complex removal easily without a Filter.
        // If your version supports store.removeAll(filter), use it.
        // Otherwise, in a production environment, use a persistent vector DB (Chroma/PGVector).
        // For this snippet, we assume removeAll is available:
        try {
            store.removeAll(dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey("file_path").isEqualTo(relativePath));
        } catch (Exception e) {
            RagLogger.warn("Could not remove file from index: " + relativePath);
        }
    }

    /**
     * Searches the index for relevant text segments based on the query.
     * Returns results grouped by file path.
     *
     * @param query      The search query.
     * @param projectId  The project identifier.
     * @param maxResults Maximum number of results to return, used from config if <=0.
     * @param minScore   Minimum similarity score threshold, used from config if <=0.
     * @return A map of file paths to lists of relevant text segments.
     */
    public Map<String, List<String>> searchGrouped(String query, String projectId, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults > 0 ? maxResults : config.getMaxSearchResults())
                .minScore(minScore > 0 ? minScore: config.getMinScore())
                .build();

        EmbeddingSearchResult<TextSegment> result = store.search(request);

        Map<String, List<String>> groupedResults = new HashMap<>();
        for (EmbeddingMatch<TextSegment> match : result.matches()) {
            String path = match.embedded().metadata().getString("file_path");
            groupedResults.computeIfAbsent(path, k -> new ArrayList<>()).add(match.embedded().text());
        }
        return groupedResults;
    }

    // --- Persistence Logic ---

    public void saveIndexToDisk(String projectId, Path projectRoot) {
        Path dataDir = projectRoot.resolve(".rag_data");
        Path indexFile = dataDir.resolve("embeddings.json");

        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

            InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);
            String json = store.serializeToJson(); // LangChain4j built-in serialization
            Files.writeString(indexFile, json);
            RagLogger.info("Saved index for project: " + projectId);
        } catch (Exception e) {
            RagLogger.error("Error saving index: " + e.getMessage());
        }
    }

    public void loadIndexFromDisk(String projectId, Path projectRoot) {
        Path indexFile = projectRoot.resolve(".rag_data").resolve("embeddings.json");
        if (Files.exists(indexFile)) {
            try {
                String json = Files.readString(indexFile);
                InMemoryEmbeddingStore<TextSegment> store = InMemoryEmbeddingStore.fromJson(json);
                projectStores.put(projectId, store);
                RagLogger.info("Loaded index from disk for: " + projectId);
            } catch (Exception e) {
                RagLogger.error("Error loading index: " + e.getMessage());
            }
        }
    }
}