package org.example.service;

import org.example.model.Chunk;
import org.example.model.ConflictFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConflictParser {
    private final File workingDirectory;

    public ConflictParser(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ConflictFile parseChunks(String relativePath) {
        ConflictFile conflictFile = new ConflictFile(relativePath);
        Path path = new File(workingDirectory, relativePath).toPath();
        try {
            List<String> lines;
            try {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (Exception utf8Ex) {
                lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
            }

            StringBuilder currentChunk = new StringBuilder();
            boolean conflicts = false;
            boolean hasSeparator = false;
            int startLine = 0;
            int endLine = 0;
            int lineCounter = 1;

            for (String line : lines) {
                if (line.startsWith("<<<<<<<")) {
                    conflicts = true;
                    hasSeparator = false;
                    currentChunk.setLength(0);
                    startLine = lineCounter;
                }

                if (conflicts) {
                    currentChunk.append(line).append("\n");
                }

                if (conflicts && line.startsWith("=======")) {
                    hasSeparator = true;
                }

                if (line.startsWith(">>>>>>>")) {
                    conflicts = false;
                    endLine = lineCounter;
                    if (hasSeparator) {
                        Chunk chunk = new Chunk(currentChunk.toString());
                        chunk.setEndLine(endLine);
                        chunk.setStartLine(startLine);
                        conflictFile.addChunk(chunk);
                    }
                    hasSeparator = false;
                }
                lineCounter++;
            }

        } catch (Exception e) {
            System.err.println("[conflicts-analyzer] Erro ao ler arquivo '" + relativePath + "': " + e.getMessage());
        }
        return conflictFile;
    }
}
