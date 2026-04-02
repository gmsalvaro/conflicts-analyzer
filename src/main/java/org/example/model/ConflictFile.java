package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class ConflictFile {
    private  List<Chunk> chunks;
    private  String filePath;


    public ConflictFile( String filePath) {
        this.filePath = filePath;
        chunks = new ArrayList<>();
    }

    public void printFile(){
        System.out.println("File: " + filePath);
        for(Chunk chunk : chunks){
            System.out.println("Chunk: " + chunk.getContent());
            System.out.println("Start Line: " + chunk.getStartLine());
            System.out.println("End Line: " + chunk.getEndLine());
        }
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }
}

