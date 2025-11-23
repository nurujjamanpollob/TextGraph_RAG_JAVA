package com.nurujjamanpollob.textenginejava.rag.context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextCollector {

    private final EmbeddingModel embeddingModel;
    // We map ProjectID -> Specific Embedding Store to keep them isolated and easily serializable
    private final Map<String, InMemoryEmbeddingStore<TextSegment>> projectStores;
    private final Gson gson;

    public ContextCollector() {
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

        // Optimized splitter for code
        List<TextSegment> segments = DocumentSplitters.recursive(1000, 150).split(doc);

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
        store.removeAll(dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey("file_path").isEqualTo(relativePath));
    }

    public Map<String, List<String>> searchGrouped(String query, String projectId) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        InMemoryEmbeddingStore<TextSegment> store = getStore(projectId);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(15)
                .minScore(0.6)
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
            System.out.println("Saved index for project: " + projectId);
        } catch (Exception e) {
            System.err.println("Error saving index: " + e.getMessage());
        }
    }

    public void loadIndexFromDisk(String projectId, Path projectRoot) {
        Path indexFile = projectRoot.resolve(".rag_data").resolve("embeddings.json");
        if (Files.exists(indexFile)) {
            try {
                String json = Files.readString(indexFile);
                InMemoryEmbeddingStore<TextSegment> store = InMemoryEmbeddingStore.fromJson(json);
                projectStores.put(projectId, store);
                System.out.println("Loaded index from disk for: " + projectId);
            } catch (Exception e) {
                System.err.println("Error loading index: " + e.getMessage());
            }
        }
    }
}