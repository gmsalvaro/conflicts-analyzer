package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MergeServiceTest {

    @Mock
    private GitProcessRunner runner;

    private static final String MERGE_HASH = "abc123def456";
    private static final String PARENT1    = "aaa111";
    private static final String PARENT2    = "bbb222";
    private static final String HEAD_HASH  = "fffff00000";

    private MergeService mergeService;

    @BeforeEach
    void setUp() {
        mergeService = new MergeService(runner, MERGE_HASH);
    }

    /**
     * Configura o mock para simular uma sequência de chamadas durante simulateMerge()
     * seguindo a ordem corrigida (Fix 1):
     *  1ª chamada → git rev-parse HEAD  (retorna HEAD_HASH)
     *  2ª chamada → git log ...         (retorna os parents)
     *  3ª chamada → git merge --abort   (no-op, retorna 0)
     *  4ª chamada → git reset --hard    (retorna 0)
     *  5ª chamada → git checkout parent1 (retorna exitCode 0)
     *  6ª chamada → git merge --no-commit (retorna mergeExitCode)
     */
    private void setupSimulateMerge(int mergeExitCode) {
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, HEAD_HASH))                                // 1: rev-parse HEAD
            .thenReturn(new ProcessResult(0, PARENT1 + " " + PARENT2))                  // 2: git log parents
            .thenReturn(new ProcessResult(0, ""))                                        // 3: merge --abort
            .thenReturn(new ProcessResult(0, ""))                                        // 4: reset --hard
            .thenReturn(new ProcessResult(0, ""))                                        // 5: checkout parent1
            .thenReturn(new ProcessResult(mergeExitCode, mergeExitCode == 0 ? "" : "CONFLICT")); // 6: merge
    }

    @Test
    void deveRetornarTrueQuandoMergeSemConflitos() {
        setupSimulateMerge(0);
        assertTrue(mergeService.simulateMerge(), "Merge limpo deve retornar true");
    }

    @Test
    void deveRetornarFalseQuandoMergeComConflitos() {
        setupSimulateMerge(1);
        assertFalse(mergeService.simulateMerge(), "Merge com conflitos deve retornar false");
    }

    @Test
    void deveLancarExcecaoParaCommitNaoMerge() {
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, HEAD_HASH))     // rev-parse HEAD
            .thenReturn(new ProcessResult(0, PARENT1));      // git log retorna só 1 parent

        assertThrows(IllegalArgumentException.class,
            () -> mergeService.simulateMerge(),
            "Deve lançar exceção para commit com apenas um parent");
    }

    @Test
    void deveRetornarListaDeArquivosComConflito() {
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, "src/Foo.java\nsrc/Bar.java\n"));

        List<String> files = mergeService.getConflictedFiles();

        assertEquals(2, files.size());
        assertEquals("src/Foo.java", files.get(0));
        assertEquals("src/Bar.java", files.get(1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaConflitos() {
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, ""));

        List<String> files = mergeService.getConflictedFiles();

        assertTrue(files.isEmpty(), "Sem conflitos, lista deve ser vazia");
    }

    @Test
    void deveRetornarArquivosCorretamenteComLinhaTerminadaEmCRLF() {
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, "src/A.java\r\nsrc/B.java\r\n"));

        List<String> files = mergeService.getConflictedFiles();

        assertEquals(2, files.size());
        assertEquals("src/A.java", files.get(0), "Não deve conter \\r no nome do arquivo");
        assertEquals("src/B.java", files.get(1), "Não deve conter \\r no nome do arquivo");
    }

    @Test
    void abortMergeDeveExecutarSemLancarExcecao() {
        // Configura simulateMerge para popular o originalHead
        setupSimulateMerge(1);
        mergeService.simulateMerge();

        // 5ª chamada → abort, 6ª → reset --hard, 7ª → checkout HEAD original
        when(runner.runProcess(any()))
            .thenReturn(new ProcessResult(0, "")) // merge --abort
            .thenReturn(new ProcessResult(0, "")) // git reset --hard HEAD
            .thenReturn(new ProcessResult(0, "")); // checkout HEAD original

        assertDoesNotThrow(() -> mergeService.abortMerge(),
            "abortMerge não deve lançar exceção");

        // Deve ter chamado runProcess pelo menos 7 vezes (4 simulateMerge + 3 abortMerge)
        verify(runner, atLeast(7)).runProcess(any());
    }
}
