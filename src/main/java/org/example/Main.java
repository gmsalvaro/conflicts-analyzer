package org.example;

import org.example.model.ConflictFile;
import org.example.model.ConflictResult;
import org.example.service.ConflictParser;
import org.example.service.GitProcessRunner;
import org.example.service.JsonSerializer;
import org.example.service.MergeService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Exit codes:
 * 0 — merge simulated successfully, no conflicts
 * 1 — conflicts found (JSON with details printed to stdout)
 * 2 — error (message printed to stderr)
 *
 * stdout → JSON result (machine-readable)
 * stderr → diagnostic / log messages (human-readable)
 */
public class Main {
    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: java -jar conflicts-analyzer.jar <commit_hash> <repo_path>");
            System.exit(2);
        }

        String hash = args[0];
        String repoPath = args[1];
        File repoDir = new File(repoPath);

        if (!repoDir.exists() || !new File(repoPath, ".git").exists()) {
            System.err.println("ERROR: Invalid repository path: " + repoPath);
            System.exit(2);
        }

        System.err.println("[conflicts-analyzer] Simulating merge for commit " + hash + " in " + repoPath);

        GitProcessRunner runner = new GitProcessRunner(repoDir);
        MergeService mergeService = new MergeService(runner, hash);
        JsonSerializer serializer = new JsonSerializer();

        try {
            boolean clean = mergeService.simulateMerge();

            if (clean) {
                System.err.println("[conflicts-analyzer] No conflicts detected.");
                ConflictResult result = new ConflictResult(hash, false, new ArrayList<>());
                System.out.println(serializer.serialize(result));
                System.exit(0);
            }

            List<String> conflictedPaths = mergeService.getConflictedFiles();
            ConflictParser parser = new ConflictParser(repoDir);
            List<ConflictFile> conflictFiles = new ArrayList<>();

            for (String path : conflictedPaths) {
                if (path.trim().isEmpty())
                    continue;
                System.err.println("[conflicts-analyzer] Conflicted file: " + path);
                conflictFiles.add(parser.parseChunks(path));
            }

            ConflictResult result = new ConflictResult(hash, true, conflictFiles);
            System.out.println(serializer.serialize(result));
            System.exit(1);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(2);
        } finally {
            mergeService.abortMerge();
        }
    }
}
