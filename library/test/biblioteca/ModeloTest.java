package biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import org.junit.jupiter.api.Test;

class ModeloTest {
    @Test
    void usuariosTemLimitesEPrazosExtras() {
        assertEquals(3, new Aluno("A", "123", "(62) 99999-0000", "a@b.com").getLimiteEmprestimos());
        assertEquals(5, new Professor("P", "123", "(62) 99999-0000", "a@b.com").getLimiteEmprestimos());
        assertEquals(2, new Externo("E", "123", "(62) 99999-0000", "a@b.com").getLimiteEmprestimos());
        assertEquals(0, new Aluno("A", "123", "(62) 99999-0000", "a@b.com").getPrazoExtra());
        assertEquals(7, new Professor("P", "123", "(62) 99999-0000", "a@b.com").getPrazoExtra());
        assertEquals(0, new Externo("E", "123", "(62) 99999-0000", "a@b.com").getPrazoExtra());
    }

    @Test
    void materiaisTemPrazosPorTipo() {
        assertEquals(14, new Livro("1", "t", 2020, 1, 1, false, "a", "i").getPrazoDias());
        assertEquals(7, new Revista("2", "t", 2020, 1, 1, false, 1, "m").getPrazoDias());
        assertEquals(5, new OutraPublicacao("3", "t", 2020, 1, 1, false, "d").getPrazoDias());
    }

    @Test
    void calculoDePenalidadePorTipo() {
        Date d = Util.criar(2026, 11, 1);
        Livro l = new Livro("1", "t", 2020, 1, 1, false, "a", "i");
        Revista r = new Revista("2", "t", 2020, 1, 1, false, 1, "m");
        OutraPublicacao o = new OutraPublicacao("3", "t", 2020, 1, 1, false, "d");
        assertNull(l.calcularPenalidade(0, d));
        assertNull(r.calcularPenalidade(-2, d));
        assertEquals(5.0, ((Multa) l.calcularPenalidade(5, d)).getValor(), 0.001);
        assertEquals(10.0, ((Multa) r.calcularPenalidade(5, d)).getValor(), 0.001);
        Suspensao s = (Suspensao) o.calcularPenalidade(5, d);
        assertEquals(10, s.getDias());
        assertEquals(Util.criar(2026, 11, 11), s.getDataFim());
    }

    @Test
    void multaQuitadaDeixaDeEstarAtiva() {
        Multa m = new Multa(10, "x", Util.criar(2026, 11, 1));
        assertTrue(m.estaAtiva());
        m.quitar();
        assertFalse(m.estaAtiva());
        assertTrue(m.isQuitada());
    }

    @Test
    void emprestimoCalculaAtraso() {
        Aluno a = new Aluno("A", "123", "(62) 99999-0000", "a@b.com");
        Livro l = new Livro("1", "t", 2020, 1, 10, false, "a", "i");
        Emprestimo e = new Emprestimo(a, l, Util.criar(2026, 11, 1)); // previsto 15/11
        assertEquals(0, e.calcularAtraso(Util.criar(2026, 11, 15)));
        assertEquals(1, e.calcularAtraso(Util.criar(2026, 11, 16)));
        assertTrue(e.estaEmAtraso(Util.criar(2026, 11, 20)));
        assertFalse(e.estaEmAtraso(Util.criar(2026, 11, 10)));
    }

    @Test
    void utilFormataEInterpretaDatasEMoeda() throws Exception {
        assertEquals("05/11/2026", Util.formatar(Util.criar(2026, 11, 5)));
        assertEquals(Util.criar(2026, 2, 28), Util.interpretar("28/02/2026"));
        assertThrows(BibliotecaException.class, () -> Util.interpretar("31/02/2026"));
        assertThrows(BibliotecaException.class, () -> Util.interpretar("abc"));
        assertEquals("R$ 1.234,50", Util.moeda(1234.5));
        assertEquals("sao paulo", Util.normalizar("São Paulo"));
        assertEquals("12345678900", Util.chave("123.456.789-00"));
    }

    @Test
    void excecaoPropriaGuardaMensagem() {
        BibliotecaException e = new BibliotecaException("erro X");
        assertEquals("erro X", e.getMessage());
    }
}
