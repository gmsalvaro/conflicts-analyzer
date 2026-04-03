package org.example.service;

import org.example.model.Chunk;
import org.example.model.ConflictFile;
import org.example.model.ConflictResult;

import java.util.List;

public class JsonSerializer {

    public String serialize(ConflictResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"commit\": ").append(quoted(result.getCommit())).append(",\n");
        sb.append("  \"hasConflicts\": ").append(result.isHasConflicts()).append(",\n");
        sb.append("  \"conflictedFiles\": ");
        serializeFiles(sb, result.getConflictedFiles());
        sb.append("\n}");
        return sb.toString();
    }

    private void serializeFiles(StringBuilder sb, List<ConflictFile> files) {
        sb.append("[\n");
        for (int i = 0; i < files.size(); i++) {
            ConflictFile file = files.get(i);
            sb.append("    {\n");
            sb.append("      \"filePath\": ").append(quoted(file.getFilePath())).append(",\n");
            sb.append("      \"conflictChunks\": ");
            serializeChunks(sb, file.getChunks());
            sb.append("\n    }");
            if (i < files.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]");
    }

    private void serializeChunks(StringBuilder sb, List<Chunk> chunks) {
        sb.append("[\n");
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            sb.append("        {\n");
            sb.append("          \"startLine\": ").append(chunk.getStartLine()).append(",\n");
            sb.append("          \"endLine\": ").append(chunk.getEndLine()).append(",\n");
            sb.append("          \"content\": ").append(quoted(chunk.getContent())).append("\n");
            sb.append("        }");
            if (i < chunks.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("      ]");
    }

    /** Escapes a string value for JSON output. */
    private String quoted(String value) {
        if (value == null)
            return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                + "\"";
    }
}
