package org.example.model;

public class Chunk {
    private String content;
    private int endLine;
    private int startLine;

    public Chunk(String content) {
        this.content = content;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getContent() {
        return content;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
}
