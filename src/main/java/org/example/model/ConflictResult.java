package org.example.model;

import java.util.List;

/**
 * Represents the full analysis result for a single commit.
 * This is the top-level object serialized to JSON on stdout.
 */
public class ConflictResult {
    private final String commit;
    private final boolean hasConflicts;
    private final List<ConflictFile> conflictedFiles;

    public ConflictResult(String commit, boolean hasConflicts, List<ConflictFile> conflictedFiles) {
        this.commit = commit;
        this.hasConflicts = hasConflicts;
        this.conflictedFiles = conflictedFiles;
    }

    public String getCommit() {
        return commit;
    }

    public boolean isHasConflicts() {
        return hasConflicts;
    }

    public List<ConflictFile> getConflictedFiles() {
        return conflictedFiles;
    }
}
