package org.example.service;

import org.example.git.Git;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeService {
    private final GitProcessRunner runner;
    private final String hashCommit;
    private String originalHead;

    public MergeService(GitProcessRunner runner, String hashCommit) {
        this.runner = runner;
        this.hashCommit = hashCommit;
    }

    private String[] getParents() {
        ProcessResult result = runner.runProcess(Git.getParents(hashCommit));
        String[] parents = result.getOutput().trim().split("\\s+");
        if (parents.length < 2 || parents[0].isEmpty()) {
            throw new IllegalArgumentException(
                    "O commit " + hashCommit + " não é um merge commit (não possui dois parents).");
        }
        return parents;
    }

    public boolean simulateMerge() {
        // 1. Salva HEAD atual para restaurar no final
        ProcessResult headResult = runner.runProcess(Git.getHead());
        originalHead = headResult.getOutput().trim();

        // 2. Obtém parents ANTES de limpar o índice (falha rápido se não for merge
        // commit)
        String[] parents = getParents();
        String parent1 = parents[0];
        String parent2 = parents[1];

        // 3. Limpa qualquer merge ou conflito pendente de execuções anteriores
        runner.runProcess(Git.abortarMerge());
        runner.runProcess(Git.resetForced());

        // 4. Faz checkout do primeiro parent (base do merge)
        ProcessResult checkoutResult = runner.runProcess(Git.checkoutSilencioso(parent1));
        if (checkoutResult.getExitCode() != 0) {
            throw new RuntimeException("Falha ao fazer checkout do parent1 (" + parent1 + "): "
                    + checkoutResult.getOutput());
        }

        // 5. Tenta mergear o segundo parent sem commitar
        ProcessResult mergeResult = runner.runProcess(Git.mergeSemCommit(parent2));
        return mergeResult.getExitCode() == 0;
    }

    public List<String> getConflictedFiles() {
        ProcessResult result = runner.runProcess(Git.listarArquivosComConflito());
        String output = result.getOutput().trim();
        if (output.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(output.split("\\r?\\n"))
                .filter(line -> !line.startsWith("warning:")
                        && !line.startsWith("error:")
                        && !line.startsWith("hint:")
                        && !line.isBlank())
                .collect(java.util.stream.Collectors.toList());
    }

    public void abortMerge() {
        runner.runProcess(Git.abortarMerge());
        runner.runProcess(Git.resetForced());
        if (originalHead != null && !originalHead.isEmpty()) {
            runner.runProcess(Git.checkoutSilencioso(originalHead));
        }
    }
}
