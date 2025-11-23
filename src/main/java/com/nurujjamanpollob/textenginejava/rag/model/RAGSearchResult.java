package com.nurujjamanpollob.textenginejava.rag.model;

/**
 * @author nurujjamanpollob
 * @apinote, contains the result of a RAG search operation.
 */
public class RAGSearchResult {

    private final String content;
    private final double score;
    private final String filePath;

    public RAGSearchResult(String content, double score, String filePath) {
        this.content = content;
        this.score = score;
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
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
                "content='" + content + '\'' +
                ", score=" + score +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
