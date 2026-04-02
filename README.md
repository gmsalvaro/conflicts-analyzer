# conflicts-analyzer

Ferramenta de linha de comando que analisa um **merge commit** de um repositório Git e detecta todos os **conflitos de merge** que ocorreram, exibindo os chunks em conflito com seus números de linha.

---

## Como funciona

Dado o hash de um merge commit, a ferramenta:

1. Obtém os dois **parents** do commit (`git log --pretty=%P -1 <hash>`)
2. Faz **checkout do parent1** (a base do merge)
3. Tenta mergear o **parent2** sem commitar (`git merge --no-commit --no-ff <parent2>`)
4. Se houve conflitos, lista os **arquivos em conflito** (`git diff --name-only --diff-filter=U`)
5. Lê cada arquivo conflitado e extrai os **chunks** delimitados pelos marcadores `<<<<<<< / ======= / >>>>>>>`
6. Exibe arquivo, conteúdo do chunk e linhas de início/fim
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
mvn clean package
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

### Exemplo

```bash
java -jar target/conflicts-analyzer-1.0-SNAPSHOT.jar \
  2324cc140b3eaef25919446f30decc85f2742d18 \
  "C:\projetos\meu-repo"
```

### Saída esperada — sem conflitos

```
Simulating merge for commit 2324cc... in repository C:\projetos\meu-repo
Merge simulated successfully. No conflicts detected.
```

### Saída esperada — com conflitos

```
Simulating merge for commit 2324cc... in repository C:\projetos\meu-repo
Merge simulated with conflicts. Conflicted files:
File: src/main/java/com/example/Foo.java
Chunk: <<<<<<< HEAD
    int x = 1;
=======
    int x = 2;
>>>>>>> feature-branch

Start Line: 42
End Line: 46
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
│   │   │   ├── Chunk.java                   # Representa um bloco de conflito
│   │   │   └── ConflictFile.java            # Representa um arquivo com conflitos
│   │   └── service/
│   │       ├── ConflictParser.java          # Lê arquivos e extrai chunks de conflito
│   │       ├── GitProcessRunner.java        # Executa processos Git via ProcessBuilder
│   │       ├── MergeService.java            # Orquestra a simulação do merge
│   │       └── ProcessResult.java           # Encapsula exitCode + output de um processo
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

Execute os testes unitários com:

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
- O repositório precisa estar em um estado limpo (sem mudanças não commitadas) antes de executar a ferramenta, pois o `git checkout` pode falhar caso contrário.
- Arquivos binários em conflito são listados mas não têm chunks extraíveis.
