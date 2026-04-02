package org.example.service;

import org.example.model.Chunk;
import org.example.model.ConflictFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConflictParser {
    private final File workingDirectory;

    public ConflictParser(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ConflictFile parseChunks(String relativePath ) {
        ConflictFile conflictFile = new ConflictFile(relativePath);
        Path path = new File(workingDirectory, relativePath).toPath();
        try{
            List<String> lines = Files.readAllLines(path);
            StringBuilder currentChunk = new StringBuilder();
            boolean conflicts = false;
            int startLine = 0;
            int endLine = 0;
            int lineCounter = 1;

            for(String line : lines) {
                if (line.startsWith("<<<<<<<")) {
                    conflicts = true;
                    currentChunk.setLength(0);
                    startLine = lineCounter;
                }

                if (conflicts) {
                    currentChunk.append(line).append("\n");
                }
                if (line.startsWith(">>>>>>>")) {
                    conflicts = false;
                    endLine = lineCounter;
                    Chunk chunk = new Chunk(currentChunk.toString());
                    chunk.setEndLine(endLine);
                    chunk.setStartLine(startLine);
                    conflictFile.addChunk(chunk);
                }
                lineCounter++;
            }

        }catch (Exception e){
                System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            }
        return conflictFile;
    }
}

