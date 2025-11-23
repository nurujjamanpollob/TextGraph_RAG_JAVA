package com.nurujjamanpollob.textenginejava.rag.model;

import java.util.HashMap;
import java.util.Map;

public class ProjectMetadata {
    // Map<RelativeFilePath, FileHash>
    private Map<String, String> fileHashes = new HashMap<>();
    private long lastIndexed;

    public Map<String, String> getFileHashes() {
        return fileHashes;
    }

    public void setFileHashes(Map<String, String> fileHashes) {
        this.fileHashes = fileHashes;
    }

    public long getLastIndexed() {
        return lastIndexed;
    }

    public void setLastIndexed(long lastIndexed) {
        this.lastIndexed = lastIndexed;
    }
}