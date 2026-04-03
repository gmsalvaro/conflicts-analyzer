# conflicts-analyzer

Ferramenta de linha de comando que analisa um **merge commit** de um repositório Git e detecta todos os **conflitos de merge**, emitindo os resultados em **JSON estruturado** no `stdout` — ideal para ser consumida como subprocesso por outro programa.

---

## Como funciona

Dado o hash de um merge commit, a ferramenta:

1. Obtém os dois **parents** do commit (`git log --pretty=%P -1 <hash>`)
2. Faz **checkout do parent1** (a base do merge)
3. Tenta mergear o **parent2** sem commitar (`git merge --no-commit --no-ff <parent2>`)
4. Se houve conflitos, lista os **arquivos em conflito** (`git diff --name-only --diff-filter=U`)
5. Lê cada arquivo conflitado e extrai os **chunks** delimitados pelos marcadores `<<<<<<< / ======= / >>>>>>>`
6. Emite um JSON completo no `stdout` com commit, arquivos e chunks
7. **Restaura o repositório** ao estado original (`git merge --abort` + `git checkout <HEAD original>`)

> O repositório sempre volta ao seu estado original após a análise.

---

## Pré-requisitos

- Java 21+
- Maven 3.8+
- Git instalado e disponível no PATH

---

## Compilação

```bash
mvn clean package -DskipTests
```

O JAR executável será gerado em `target/conflicts-analyzer-1.0-SNAPSHOT.jar`.

---

## Uso

```
java -jar target/conflicts-analyzer-1.0-SNAPSHOT.jar <hash_do_commit> <caminho_do_repositório>
```

| Argumento               | Descrição                                              |
|-------------------------|--------------------------------------------------------|
| `<hash_do_commit>`      | Hash SHA do merge commit a ser analisado               |
| `<caminho_do_repositório>` | Caminho absoluto para o repositório Git local       |

---

## Saída (stdout / stderr)

| Stream   | Conteúdo                                                         |
|----------|------------------------------------------------------------------|
| `stdout` | JSON estruturado com o resultado da análise                      |
| `stderr` | Mensagens de log e diagnóstico (prefixo `[conflicts-analyzer]`) |

### Exit codes

| Código | Significado                          |
|--------|--------------------------------------|
| `0`    | Merge sem conflitos                  |
| `1`    | Conflitos encontrados (JSON no stdout) |
| `2`    | Erro de execução (mensagem no stderr) |

---

## Formato JSON de saída

### Sem conflitos (exit 0)

```json
{
  "commit": "2324cc140b3eaef25919446f30decc85f2742d18",
  "hasConflicts": false,
  "conflictedFiles": []
}
```

### Com conflitos (exit 1)

```json
{
  "commit": "2324cc140b3eaef25919446f30decc85f2742d18",
  "hasConflicts": true,
  "conflictedFiles": [
    {
      "filePath": "src/main/java/com/example/Foo.java",
      "conflictChunks": [
        {
          "startLine": 42,
          "endLine": 46,
          "content": "<<<<<<< HEAD\n    int x = 1;\n=======\n    int x = 2;\n>>>>>>> feature-branch\n"
        }
      ]
    }
  ]
}
```

---

## Consumindo como subprocesso (exemplo em Java)

```java
ProcessBuilder pb = new ProcessBuilder(
    "java", "-jar", "conflicts-analyzer.jar",
    commitHash, repoPath
);
pb.redirectError(ProcessBuilder.Redirect.INHERIT); // stderr → console
Process proc = pb.start();

String json = new String(proc.getInputStream().readAllBytes());
int exitCode = proc.waitFor();

if (exitCode == 0) {
    // sem conflitos
} else if (exitCode == 1) {
    // parsear json com os conflitos
} else {
    // erro — verificar stderr
}
```

---

## Estrutura do projeto

```
conflicts-analyzer/
├── src/
│   ├── main/java/org/example/
│   │   ├── Main.java                        # Ponto de entrada
│   │   ├── git/
│   │   │   └── Git.java                     # Fábrica de comandos Git
│   │   ├── model/
│   │   │   ├── Chunk.java                   # Represents a conflict block
│   │   │   ├── ConflictFile.java            # Represents a file with conflicts
│   │   │   └── ConflictResult.java          # Top-level result (commit + files)
│   │   └── service/
│   │       ├── ConflictParser.java          # Lê arquivos e extrai chunks
│   │       ├── GitProcessRunner.java        # Executa processos Git
│   │       ├── JsonSerializer.java          # Serializa resultado para JSON
│   │       ├── MergeService.java            # Orquestra a simulação do merge
│   │       └── ProcessResult.java           # Encapsula exitCode + output
│   └── test/java/org/example/
│       ├── model/
│       │   └── ConflictFileTest.java
│       └── service/
│           ├── ConflictParserTest.java
│           └── MergeServiceTest.java
└── pom.xml
```

---

## Testes

```bash
mvn test
```

### Cobertura dos testes

| Classe            | Cenários testados                                                              |
|-------------------|--------------------------------------------------------------------------------|
| `ConflictParser`  | 1 chunk, múltiplos chunks, sem conflito, arquivo vazio, rastreamento de path   |
| `MergeService`    | Merge limpo, merge com conflito, commit não-merge, CRLF no Windows, restauração do HEAD |
| `ConflictFile`    | Adição de chunks, lista vazia inicial, getter de path                          |

---

## Limitações conhecidas

- Suporta apenas **merge commits** (commits com dois parents). Commits normais ou de rebase resultarão em erro informativo.
- O repositório precisa estar em um estado limpo (sem mudanças não commitadas) antes de executar a ferramenta.
- Arquivos binários em conflito são listados mas não têm chunks extraíveis.
