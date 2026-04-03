package org.example.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GitProcessRunner {
    private final File workingDirectory;

    public GitProcessRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ProcessResult runProcess(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> stderrFuture = executor.submit(() -> {
                try (BufferedReader err = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    while (err.readLine() != null) {
                        /* descarta warnings do stderr */ }
                } catch (Exception ignored) {
                    /* ignorado propositalmente */ }
            });

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            stderrFuture.get();
            executor.shutdown();

            int exitCode = process.waitFor();
            return new ProcessResult(exitCode, output.toString());

        } catch (Exception e) {
            // Em caso de erro grave (ex: Git não instalado), retorna como falha
            return new ProcessResult(1, "Erro interno ao executar o comando: " + e.getMessage());
        }
    }
}
