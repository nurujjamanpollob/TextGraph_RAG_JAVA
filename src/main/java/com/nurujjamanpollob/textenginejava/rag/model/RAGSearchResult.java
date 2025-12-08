package com.nurujjamanpollob.textenginejava.rag.model;

import java.util.List;

/**
 * @author nurujjamanpollob
 * @apinote, contains the result of a RAG search operation.
 */
public class RAGSearchResult {

    private final List<String> contents;
    private final double score;
    private final String filePath;

    public RAGSearchResult(List<String> contents, double score, String filePath) {
        this.contents = contents;
        this.score = score;
        this.filePath = filePath;
    }


    public List<String> contents() {
        return List.of(filePath);
    }

    public double getScore() {
        return score;
    }
    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "RAGSearchResult{" +
                "contents=" + contents +
                ", score=" + score +
                ", filePath='" + filePath + '\'' +
                '}';
    }

}
