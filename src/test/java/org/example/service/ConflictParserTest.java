package org.example.service;

import org.example.model.Chunk;
import org.example.model.ConflictFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConflictParserTest {

    @TempDir
    Path tempDir;

    private File writeFile(String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content);
        return file.toFile();
    }

    @Test
    void deveCapturarUmChunkDeConflito() throws IOException {
        String content = """
                linha normal 1
                linha normal 2
                <<<<<<< HEAD
                código da branch atual
                =======
                código da outra branch
                >>>>>>> feature-branch
                linha normal 3
                """;

        writeFile("Foo.java", content);
        ConflictParser parser = new ConflictParser(tempDir.toFile());
        ConflictFile result = parser.parseChunks("Foo.java");

        List<Chunk> chunks = result.getChunks();
        assertEquals(1, chunks.size(), "Deve detectar exatamente 1 chunk de conflito");

        Chunk chunk = chunks.get(0);
        assertEquals(3, chunk.getStartLine(), "Start line deve ser a linha do <<<<<<<");
        assertEquals(7, chunk.getEndLine(),   "End line deve ser a linha do >>>>>>>");
        assertTrue(chunk.getContent().contains("<<<<<<< HEAD"));
        assertTrue(chunk.getContent().contains("código da branch atual"));
        assertTrue(chunk.getContent().contains("código da outra branch"));
        assertTrue(chunk.getContent().contains(">>>>>>> feature-branch"));
    }

    @Test
    void deveCapturarMultiplosChunksDeConflito() throws IOException {
        String content = """
                import A;
                <<<<<<< HEAD
                import B;
                =======
                import C;
                >>>>>>> other
                public class Foo {
                <<<<<<< HEAD
                    int x = 1;
                =======
                    int x = 2;
                >>>>>>> other
                }
                """;

        writeFile("Multi.java", content);
        ConflictParser parser = new ConflictParser(tempDir.toFile());
        ConflictFile result = parser.parseChunks("Multi.java");

        assertEquals(2, result.getChunks().size(), "Deve detectar 2 chunks de conflito");
        assertEquals(2,  result.getChunks().get(0).getStartLine());
        assertEquals(6,  result.getChunks().get(0).getEndLine());
        assertEquals(8,  result.getChunks().get(1).getStartLine());
        assertEquals(12, result.getChunks().get(1).getEndLine());
    }

    @Test
    void deveRetornarSemChunksParaArquivoSemConflito() throws IOException {
        String content = """
                public class Clean {
                    public void run() {
                        System.out.println("ok");
                    }
                }
                """;

        writeFile("Clean.java", content);
        ConflictParser parser = new ConflictParser(tempDir.toFile());
        ConflictFile result = parser.parseChunks("Clean.java");

        assertTrue(result.getChunks().isEmpty(), "Arquivo sem conflito não deve ter chunks");
    }

    @Test
    void deveRetornarSemChunksParaArquivoVazio() throws IOException {
        writeFile("Empty.java", "");
        ConflictParser parser = new ConflictParser(tempDir.toFile());
        ConflictFile result = parser.parseChunks("Empty.java");

        assertTrue(result.getChunks().isEmpty(), "Arquivo vazio não deve ter chunks");
    }

    @Test
    void deveRastrearCaminhoDoArquivoCorretamente() throws IOException {
        writeFile("Path.java", "// sem conflito");
        ConflictParser parser = new ConflictParser(tempDir.toFile());
        ConflictFile result = parser.parseChunks("Path.java");

        assertEquals("Path.java", result.getFilePath());
    }
}
