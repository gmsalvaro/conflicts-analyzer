package org.example.service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class GitProcessRunner {
    private final File workingDirectory;

    public GitProcessRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ProcessResult runProcess(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            int exitCode = process.waitFor();
            return new ProcessResult(exitCode, output.toString());

            } catch (Exception e) {
            // Em caso de erro grave (ex: Git não instalado), tratamos como falha (Exit Code 1)
            return new ProcessResult(1, "Erro interno ao executar o comando: " + e.getMessage());
        }
    }
}
