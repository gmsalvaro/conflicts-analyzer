package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConflictFileTest {

    @Test
    void deveArmazenarCaminhoDoArquivo() {
        ConflictFile file = new ConflictFile("src/main/Foo.java");
        assertEquals("src/main/Foo.java", file.getFilePath());
    }

    @Test
    void deveAdicionarChunksCorretamente() {
        ConflictFile file = new ConflictFile("Foo.java");

        Chunk c1 = new Chunk("<<<<<<< HEAD\nA\n=======\nB\n>>>>>>> branch\n");
        c1.setStartLine(1);
        c1.setEndLine(5);

        Chunk c2 = new Chunk("<<<<<<< HEAD\nC\n=======\nD\n>>>>>>> branch\n");
        c2.setStartLine(10);
        c2.setEndLine(14);

        file.addChunk(c1);
        file.addChunk(c2);

        List<Chunk> chunks = file.getChunks();
        assertEquals(2, chunks.size());
        assertEquals(1,  chunks.get(0).getStartLine());
        assertEquals(5,  chunks.get(0).getEndLine());
        assertEquals(10, chunks.get(1).getStartLine());
        assertEquals(14, chunks.get(1).getEndLine());
    }

    @Test
    void deveInicializarComListaVazia() {
        ConflictFile file = new ConflictFile("Empty.java");
        assertNotNull(file.getChunks());
        assertTrue(file.getChunks().isEmpty());
    }
}
