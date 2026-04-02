package org.example;

import org.example.model.ConflictFile;
import org.example.service.ConflictParser;
import org.example.service.GitProcessRunner;
import org.example.service.MergeService;

import java.io.File;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        if(args.length < 2){
            System.out.println("Usage: java -jar merge-simulator.jar <hash_commit> <repo_path>");
            System.exit(1);
        }

        String hash = args[0];
        String repoPath = args[1];
        File file = new File(repoPath);

        if(!file.exists() || !new File(repoPath, ".git").exists()){
            System.out.println("Invalid repository path: " + repoPath);
            System.exit(1);
        }
        System.out.println("Simulating merge for commit " + hash + " in repository " + repoPath);
        GitProcessRunner runner = new GitProcessRunner(file);
        MergeService mergeService = new MergeService(runner, hash);
        try{
            if(mergeService.simulateMerge()){
                System.out.println("Merge simulated successfully. No conflicts detected.");
            } else {
                System.out.println("Merge simulated with conflicts. Conflicted files:");

                List<String> conflictedFiles = mergeService.getConflictedFiles();
                ConflictParser parser = new ConflictParser(file);
                for(String conflictedFile : conflictedFiles){
                    if(conflictedFile.trim().isEmpty()){
                        continue;
                    }
                    ConflictFile fileMapped = parser.parseChunks(conflictedFile);
                    fileMapped.printFile();
                }
            }
        } catch (Exception e){
            System.out.println("An error occurred during merge simulation: " + e.getMessage());
        } finally {
            mergeService.abortMerge();
        }
    }
}
