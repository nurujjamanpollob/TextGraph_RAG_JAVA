package com.nurujjamanpollob.textenginejava.rag.config;

import com.nurujjamanpollob.textenginejava.rag.utils.RagLogger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RagConfig {

    private static RagConfig instance;

    // Default values
    private int chunkSize = 1000;
    private int overlapSize = 150;
    private int maxFileSize = 10 * 1024 * 1024; // 10MB
    private double minScore = 0.6;
    private int maxSearchResults = 15;

    private RagConfig() {
        loadConfiguration();
    }

    public static synchronized RagConfig getInstance() {
        if (instance == null) {
            instance = new RagConfig();
        }
        return instance;
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("rag-config.properties")) {
            if (input != null) {
                props.load(input);
                RagLogger.info("Configuration loaded from rag-config.properties");
            } else {
                RagLogger.warn("Configuration file not found, using default values");
            }
        } catch (IOException e) {
            RagLogger.error("Error loading configuration: " + e.getMessage());
        }

        this.chunkSize = parseInt(props.getProperty("chunk.size"), chunkSize);
        this.overlapSize = parseInt(props.getProperty("overlap.size"), overlapSize);
        this.maxFileSize = parseInt(props.getProperty("max.file.size"), maxFileSize);
        this.minScore = parseDouble(props.getProperty("min.score"), minScore);
        this.maxSearchResults = parseInt(props.getProperty("max.search.results"), maxSearchResults);
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null) return defaultValue;
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    public int getChunkSize() { return chunkSize; }
    public int getOverlapSize() { return overlapSize; }
    public int getMaxFileSize() { return maxFileSize; }
    public double getMinScore() { return minScore; }
    public int getMaxSearchResults() { return maxSearchResults; }
}