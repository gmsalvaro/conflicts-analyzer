package org.example.git;

public class Git {
    public Git(){}

    /**
     * Retorna os hashes dos parents de um commit (para merge commits, retorna dois parents).
     * Formato de saída: "parent1 parent2"
     */
    public static String[] getParents(String hashDoCommit) {
        return new String[]{ "git", "log", "--pretty=%P", "-1", hashDoCommit };
    }

    /**
     * Retorna o hash do commit atual (HEAD).
     */
    public static String[] getHead() {
        return new String[]{ "git", "rev-parse", "HEAD" };
    }

    /**
     * Prepara o comando para mudar de branch silenciosamente (-q).
     */
    public static String[] checkoutSilencioso(String nomeDaBranch) {
        return new String[]{ "git", "checkout", nomeDaBranch, "-q" };
    }

    /**
     * Prepara o comando para iniciar o merge travando o commit automático e o avanço rápido.
     */
    public static String[] mergeSemCommit(String hashDoCommit) {
        return new String[]{ "git", "merge", "--no-commit", "--no-ff", hashDoCommit };
    }

    /**
     * Prepara o comando que lista apenas os Nomes dos arquivos quebrados (filtro Unmerged = U).
     */
    public static String[] listarArquivosComConflito() {
        return new String[]{ "git", "diff", "--name-only", "--diff-filter=U" };
    }

    /**
     * Prepara o comando para desfazer a bagunça e voltar o repositório ao normal.
     */
    public static String[] abortarMerge() {
        return new String[]{ "git", "merge", "--abort" };
    }
}
