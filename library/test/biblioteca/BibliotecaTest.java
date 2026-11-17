package biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BibliotecaTest {
    @TempDir
    Path pasta;

    Biblioteca bib;
    Livro livro;
    Revista revista;
    OutraPublicacao outra;
    Material atlas;
    Aluno aluno;
    Professor professor;
    Externo externo;

    static final Date D02 = Util.criar(2026, 11, 2);

    @BeforeEach
    void preparar() throws BibliotecaException {
        bib = new Biblioteca(pasta.resolve("teste.dat").toString());
        livro = new Livro("L1", "Java Basico", 2020, 5, 100.0, false, "Autor Um", "111");
        revista = new Revista("R1", "Revista Teste", 2026, 3, 20.0, false, 10, "Mensal");
        outra = new OutraPublicacao("O1", "Jornal", 2026, 3, 10.0, false, "Jornal diario");
        atlas = new OutraPublicacao("O2", "Atlas", 2020, 1, 50.0, true, "Consulta local");
        bib.cadastrarMaterial(livro);
        bib.cadastrarMaterial(revista);
        bib.cadastrarMaterial(outra);
        bib.cadastrarMaterial(atlas);
        aluno = new Aluno("Ana", "202610001", "(62) 99999-0001", "ana@exemplo.com");
        professor = new Professor("Carlos", "123.456.789-00", "(62) 98888-0003", "carlos@exemplo.com");
        externo = new Externo("Marina", "987.654.321-00", "(62) 97777-0004", "marina@exemplo.com");
        bib.cadastrarUsuario(aluno);
        bib.cadastrarUsuario(professor);
        bib.cadastrarUsuario(externo);
    }

    private void assertErro(String trecho, Executable acao) {
        BibliotecaException e = assertThrows(BibliotecaException.class, acao::executar);
        assertTrue(e.getMessage().toLowerCase().contains(trecho.toLowerCase()),
                "mensagem esperada com \"" + trecho + "\" mas foi: " + e.getMessage());
    }

    interface Executable {
        void executar() throws Exception;
    }

    // ---------------------------------------------------------------- Cadastros

    @Test
    void codigoDeMaterialDuplicadoEhRejeitado() {
        assertErro("já existe", () -> bib.cadastrarMaterial(new Livro("l1", "Outro", 2020, 1, 10, false, "A", "1")));
    }

    @Test
    void materialInvalidoEhRejeitado() {
        assertErro("título", () -> bib.cadastrarMaterial(new Livro("X", " ", 2020, 1, 10, false, "A", "1")));
        assertErro("ano", () -> bib.cadastrarMaterial(new Livro("X", "T", 3000, 1, 10, false, "A", "1")));
        assertErro("quantidade", () -> bib.cadastrarMaterial(new Livro("X", "T", 2020, 0, 10, false, "A", "1")));
        assertErro("valor", () -> bib.cadastrarMaterial(new Livro("X", "T", 2020, 1, 0, false, "A", "1")));
        assertErro("autor", () -> bib.cadastrarMaterial(new Livro("X", "T", 2020, 1, 10, false, "", "1")));
        assertErro("edição", () -> bib.cadastrarMaterial(new Revista("X", "T", 2020, 1, 10, false, 0, "Mensal")));
    }

    @Test
    void cpfOuMatriculaDuplicadoEhRejeitadoMesmoComPontuacao() {
        assertErro("já existe", () -> bib.cadastrarUsuario(
                new Externo("Outro", "12345678900", "(62) 91111-1111", "o@exemplo.com")));
    }

    @Test
    void usuarioInvalidoEhRejeitado() {
        assertErro("e-mail", () -> bib.cadastrarUsuario(new Aluno("Z", "999999", "(62) 91111-1111", "sem-arroba")));
        assertErro("telefone", () -> bib.cadastrarUsuario(new Aluno("Z", "999999", "123", "z@exemplo.com")));
        assertErro("nome", () -> bib.cadastrarUsuario(new Aluno("", "999999", "(62) 91111-1111", "z@exemplo.com")));
    }

    @Test
    void alterarMaterialAtualizaDadosEMantemEmprestimos() throws Exception {
        bib.registrarEmprestimo(aluno, livro, D02);
        bib.alterarMaterial(new Livro("L1", "Java Avancado", 2021, 4, 120.0, false, "Autor Dois", "222"));
        assertEquals("Java Avancado", livro.getTitulo());
        assertEquals("Autor Dois", livro.getAutor());
        assertEquals(1, livro.getExemplaresEmprestados());
        bib.registrarEmprestimo(professor, livro, D02);
        assertErro("não pode ser menor", () -> bib.alterarMaterial(new Livro("L1", "J", 2021, 1, 120.0, false, "A", "2")));
        assertErro("tipo", () -> bib.alterarMaterial(new Revista("L1", "J", 2021, 3, 120.0, false, 1, "M")));
    }

    @Test
    void alterarUsuarioMudaContatoMasValida() throws Exception {
        bib.alterarUsuario("202610001", "Ana Maria", "(62) 90000-0000", "ana.maria@exemplo.com");
        assertEquals("Ana Maria", aluno.getNome());
        assertErro("e-mail", () -> bib.alterarUsuario("202610001", "Ana", "(62) 90000-0000", "ruim"));
        assertEquals("Ana Maria", aluno.getNome());
    }

    @Test
    void buscaDeInexistentesLancaExcecao() {
        assertErro("não encontrado", () -> bib.buscarMaterial("ZZZ"));
        assertErro("não encontrado", () -> bib.buscarUsuario("000"));
        assertErro("não encontrado", () -> bib.buscarEmprestimo(99999));
    }

    // ---------------------------------------------------------------- Prazos (RN01)

    @Test
    void prazosDeDevolucaoPorTipoDeMaterialEUsuario() throws Exception {
        assertEquals(Util.criar(2026, 11, 16), bib.registrarEmprestimo(aluno, livro, D02).getDataPrevista());
        assertEquals(Util.criar(2026, 11, 9), bib.registrarEmprestimo(aluno, revista, D02).getDataPrevista());
        assertEquals(Util.criar(2026, 11, 7), bib.registrarEmprestimo(aluno, outra, D02).getDataPrevista());
        // prazo professor
        assertEquals(Util.criar(2026, 11, 23), bib.registrarEmprestimo(professor, livro, D02).getDataPrevista());
        assertEquals(Util.criar(2026, 11, 16), bib.registrarEmprestimo(professor, revista, D02).getDataPrevista());
        assertEquals(Util.criar(2026, 11, 14), bib.registrarEmprestimo(professor, outra, D02).getDataPrevista());
    }

    // ---------------------------------------------------------------- Empréstimo (RF03, RN03-RN05)

    @Test
    void emprestimoAtualizaDisponibilidade() throws Exception {
        assertEquals(5, livro.getExemplaresDisponiveis());
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        assertEquals(4, livro.getExemplaresDisponiveis());
        assertTrue(e.estaEmAberto());
        assertEquals(1, aluno.contarEmprestimosEmAberto());
    }

    @Test
    void materialSomenteConsultaNaoPodeSerEmprestado() {
        assertErro("consulta local", () -> bib.registrarEmprestimo(aluno, atlas, D02));
    }

    @Test
    void semExemplarDisponivelBloqueia() throws Exception {
        Revista unica = new Revista("R9", "Unica", 2026, 1, 10.0, false, 1, "Mensal");
        bib.cadastrarMaterial(unica);
        bib.registrarEmprestimo(aluno, unica, D02);
        assertErro("exemplar disponível", () -> bib.registrarEmprestimo(professor, unica, D02));
    }

    @Test
    void limiteDeEmprestimosPorTipoDeUsuario() throws Exception {
        for (int i = 0; i < 3; i++) {
            bib.registrarEmprestimo(aluno, livro, D02);
        }
        assertErro("limite de 3", () -> bib.registrarEmprestimo(aluno, livro, D02));
        for (int i = 0; i < 2; i++) {
            bib.registrarEmprestimo(externo, livro, D02);
        }
        assertErro("limite de 2", () -> bib.registrarEmprestimo(externo, revista, D02));
        assertEquals(5, professor.getLimiteEmprestimos());
    }

    @Test
    void usuarioComEmprestimoEmAtrasoNaoPodeNovoEmprestimo() throws Exception {
        bib.registrarEmprestimo(aluno, outra, D02); // previsto 07/11
        assertErro("atraso", () -> bib.registrarEmprestimo(aluno, livro, Util.criar(2026, 11, 8)));
        // sem atraso no dia previsto
        assertNotNull(bib.registrarEmprestimo(aluno, livro, Util.criar(2026, 11, 7)));
    }

    @Test
    void usuarioInativoNaoPodeEmprestar() throws Exception {
        bib.inativarUsuario("202610001");
        assertErro("inativo", () -> bib.registrarEmprestimo(aluno, livro, D02));
        bib.reativarUsuario("202610001");
        assertNotNull(bib.registrarEmprestimo(aluno, livro, D02));
    }

    // ---------------------------------------------------------------- Devolução (RF04, RN02, RN06)

    @Test
    void devolucaoNoPrazoNaoGeraPenalidade() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        ArrayList<Penalidade> p = bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 16));
        assertTrue(p.isEmpty());
        assertEquals(Emprestimo.DEVOLVIDO, e.getSituacao());
        assertEquals(5, livro.getExemplaresDisponiveis());
        assertEquals(0, e.calcularAtraso());
        assertTrue(aluno.estaApto(Util.criar(2026, 11, 16)));
    }

    @Test
    void atrasoDeLivroGeraMultaDeUmRealPorDia() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02); // previsto 16/11
        ArrayList<Penalidade> p = bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 19));
        assertEquals(1, p.size());
        assertEquals(3, e.calcularAtraso());
        Multa m = (Multa) p.get(0);
        assertEquals(3.00, m.getValor(), 0.001);
        assertFalse(m.isQuitada());
        assertTrue(aluno.temMultaPendente());
    }

    @Test
    void atrasoDeRevistaGeraMultaDeDoisReaisPorDia() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, revista, D02); // previsto 09/11
        Multa m = (Multa) bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 11)).get(0);
        assertEquals(4.00, m.getValor(), 0.001);
    }

    @Test
    void atrasoDeOutraPublicacaoGeraSuspensaoDeDoisDiasPorDia() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, outra, D02); // previsto 07/11
        Date devolucao = Util.criar(2026, 11, 10);
        Suspensao s = (Suspensao) bib.registrarDevolucao(e, EstadoMaterial.BOM, devolucao).get(0);
        assertEquals(6, s.getDias());
        assertEquals(Util.criar(2026, 11, 16), s.getDataFim());
        assertTrue(s.estaVigente(Util.criar(2026, 11, 15)));
        assertFalse(s.estaVigente(Util.criar(2026, 11, 16)));
        assertFalse(aluno.temMultaPendente());
        assertErro("suspenso", () -> bib.registrarEmprestimo(aluno, livro, Util.criar(2026, 11, 12)));
        assertNotNull(bib.registrarEmprestimo(aluno, livro, Util.criar(2026, 11, 16)));
    }

    @Test
    void materialDanificadoGeraMultaDeCinquentaPorCento() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        ArrayList<Penalidade> p = bib.registrarDevolucao(e, EstadoMaterial.DANIFICADO, Util.criar(2026, 11, 10));
        assertEquals(1, p.size());
        assertEquals(50.0, ((Multa) p.get(0)).getValor(), 0.001);
        assertEquals(5, livro.getExemplaresDisponiveis());
    }

    @Test
    void atrasoMaisDanoGeramDuasPenalidades() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        ArrayList<Penalidade> p = bib.registrarDevolucao(e, EstadoMaterial.DANIFICADO, Util.criar(2026, 11, 18));
        assertEquals(2, p.size());
        assertEquals(2, e.getPenalidades().size());
        assertEquals(2, aluno.getPenalidades().size());
    }

    @Test
    void materialExtraviadoGeraMultaIntegralERetiraExemplar() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        ArrayList<Penalidade> p = bib.registrarDevolucao(e, EstadoMaterial.EXTRAVIADO, Util.criar(2026, 11, 20));
        assertEquals(1, p.size());
        assertEquals(100.0, ((Multa) p.get(0)).getValor(), 0.001);
        assertEquals(Emprestimo.EXTRAVIADO, e.getSituacao());
        assertEquals(4, livro.getQuantidadeExemplares());
        assertEquals(4, livro.getExemplaresDisponiveis());
    }

    @Test
    void devolucaoInvalidaEhRejeitada() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        assertErro("anterior", () -> bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 1)));
        bib.registrarDevolucao(e, EstadoMaterial.BOM, D02);
        assertErro("já foi encerrado", () -> bib.registrarDevolucao(e, EstadoMaterial.BOM, D02));
        assertEquals(5, livro.getExemplaresDisponiveis());
    }

    // ---------------------------------------------------------------- Multas

    @Test
    void quitacaoLiberaUsuarioParaNovosEmprestimos() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 19));
        Date depois = Util.criar(2026, 11, 20);
        assertErro("multa pendente", () -> bib.registrarEmprestimo(aluno, livro, depois));
        Multa m = aluno.getMultasPendentes().get(0);
        bib.registrarQuitacao(aluno, m.getCodigo());
        assertTrue(m.isQuitada());
        assertNotNull(m.getDataQuitacao());
        assertEquals("Regular", aluno.getSituacao(depois));
        assertNotNull(bib.registrarEmprestimo(aluno, livro, depois));
        assertErro("já foi quitada", () -> bib.registrarQuitacao(m));
        assertErro("não encontrada", () -> bib.registrarQuitacao(aluno, 123456));
    }

    // ---------------------------------------------------------------- Remoção / inativação

    @Test
    void remocaoDeMaterialComEmprestimoEmAbertoEhBloqueada() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        assertErro("empréstimo em aberto", () -> bib.removerMaterial("L1"));
        bib.registrarDevolucao(e, EstadoMaterial.BOM, D02);
        bib.removerMaterial("L1");
        assertNull(bib.buscarMaterialOuNull("L1"));
    }

    @Test
    void inativacaoDeUsuarioBloqueadaComEmprestimoOuMulta() throws Exception {
        Emprestimo e = bib.registrarEmprestimo(aluno, livro, D02);
        assertErro("empréstimo em aberto", () -> bib.inativarUsuario("202610001"));
        bib.registrarDevolucao(e, EstadoMaterial.BOM, Util.criar(2026, 11, 19));
        assertErro("multa pendente", () -> bib.inativarUsuario("202610001"));
        bib.registrarQuitacao(aluno.getMultasPendentes().get(0));
        bib.inativarUsuario("202610001");
        assertFalse(aluno.isAtivo());
        assertErro("já está inativo", () -> bib.inativarUsuario("202610001"));
    }

    // ---------------------------------------------------------------- Consultas (RF05)

    @Test
    void consultaDeAcervoPorTituloAutorTipoEDisponibilidade() throws Exception {
        assertEquals(1, bib.consultarAcervo("BÁSICO", "", false).size());
        assertEquals(1, bib.consultarAcervo("basico", "", false).size());
        assertEquals(1, bib.consultarAcervo("autor um", "", false).size());
        assertEquals(1, bib.consultarAcervo("", "Livro", false).size());
        assertEquals(2, bib.consultarAcervo("", "Outra publicação", false).size());
        assertEquals(4, bib.consultarAcervo("", "Todos", false).size());
        assertTrue(bib.consultarAcervo("inexistente", "", false).isEmpty());

        Revista unica = new Revista("R9", "Unica", 2026, 1, 10.0, false, 1, "Mensal");
        bib.cadastrarMaterial(unica);
        bib.registrarEmprestimo(aluno, unica, D02);
        assertFalse(bib.consultarAcervo("", "", true).contains(unica));
        assertTrue(bib.consultarAcervo("", "", false).contains(unica));
        assertEquals(Util.criar(2026, 11, 9), bib.getPrevisaoDevolucao(unica));
        assertNull(bib.getPrevisaoDevolucao(livro));
    }

    @Test
    void consultaDeAcervoPorAssunto() throws Exception {
        livro.setAssunto("Programação");
        revista.setAssunto("Tecnologia");
        assertEquals(1, bib.consultarAcervo("programacao", "", false).size());
        assertTrue(bib.consultarAcervo("PROGRAMAÇÃO", "", false).contains(livro));
        assertEquals(1, bib.consultarAcervo("tecnologia", "Revista", false).size());
        assertTrue(bib.consultarAcervo("tecnologia", "Livro", false).isEmpty());
        assertEquals("", outra.getAssunto());

        Livro novo = new Livro("L1", "Java Basico", 2020, 5, 100.0, false, "Autor Um", "111");
        novo.setAssunto("Algoritmos");
        bib.alterarMaterial(novo);
        assertEquals("Algoritmos", livro.getAssunto());
        assertEquals(1, bib.consultarAcervo("algoritmos", "", false).size());
        assertTrue(bib.consultarAcervo("programacao", "", false).isEmpty());
    }

    @Test
    void operacoesDoBibliotecarioEDoUsuario() throws Exception {
        Bibliotecario bibliotecario = new Bibliotecario("Carla", "111.222.333-44", bib);
        Livro novo = new Livro("L9", "Redes", 2021, 2, 80.0, false, "Autor Dois", "222");
        bibliotecario.cadastrarMaterial(novo);
        assertSame(novo, bibliotecario.consultarMaterial("L9"));
        assertSame(aluno, bibliotecario.consultarUsuario(aluno.getCpfMatricula()));
        assertTrue(bibliotecario.consultarAcervo("redes", "", false).contains(novo));

        bibliotecario.editarUsuario(aluno.getCpfMatricula(), "Ana Maria", "(62) 99999-0001", "ana@exemplo.com");
        assertEquals("Ana Maria", aluno.getNome());

        bibliotecario.registrarEmprestimo(aluno, novo, D02);
        bibliotecario.registrarEmprestimo(professor, livro, D02);
        assertEquals(2, bibliotecario.consultarEmprestimos(null, null, null, null, "", D02).size());
        assertEquals(1, aluno.consultarEmprestimos(bib, null, null, null, "", D02).size());
        assertEquals(1, aluno.consultarAcervo(bib, "redes", "", true).size());

        bibliotecario.removerUsuario(externo.getCpfMatricula());
        assertFalse(externo.isAtivo());
        bibliotecario.removerMaterial(atlas.getCodigo());
        assertNull(bib.buscarMaterialOuNull("O2"));
    }

    @Test
    void consultaDeHistoricoComFiltros() throws Exception {
        Emprestimo e1 = bib.registrarEmprestimo(aluno, livro, D02);
        Emprestimo e2 = bib.registrarEmprestimo(professor, revista, Util.criar(2026, 11, 5));
        bib.registrarDevolucao(e1, EstadoMaterial.BOM, Util.criar(2026, 11, 6));
        Date ref = Util.criar(2026, 11, 19); // previsto 19/11

        assertEquals(2, bib.consultarHistorico(null, null, null, null, null, ref).size());
        assertEquals(1, bib.consultarHistorico(aluno, null, null, null, null, ref).size());
        assertEquals(1, bib.consultarHistorico(null, revista, null, null, null, ref).size());
        assertEquals(1, bib.consultarHistorico(null, null, Util.criar(2026, 11, 4), Util.criar(2026, 11, 30), null, ref).size());
        assertEquals(1, bib.consultarHistorico(null, null, null, null, "Devolvido", ref).size());
        assertEquals(1, bib.consultarHistorico(null, null, null, null, "Em aberto", ref).size());
        assertTrue(bib.consultarHistorico(null, null, null, null, "Em atraso", ref).isEmpty());
        // 20/11: em atraso e em aberto
        Date tarde = Util.criar(2026, 11, 20);
        assertEquals(1, bib.consultarHistorico(null, null, null, null, "Em atraso", tarde).size());
        assertEquals(1, bib.consultarHistorico(null, null, null, null, "Em aberto", tarde).size());
        assertEquals(Emprestimo.EM_ATRASO, e2.getSituacaoExibicao(tarde));
    }

    // ---------------------------------------------------------------- Códigos automáticos e arquivos

    @Test
    void codigosDeEmprestimoEPenalidadeSaoSequenciais() throws Exception {
        Emprestimo e1 = bib.registrarEmprestimo(aluno, livro, D02);
        Emprestimo e2 = bib.registrarEmprestimo(professor, livro, D02);
        assertEquals(e1.getCodigo() + 1, e2.getCodigo());
        Multa m1 = new Multa(1, "a", D02);
        Multa m2 = new Multa(1, "b", D02);
        assertEquals(m1.getCodigo() + 1, m2.getCodigo());
    }

    @Test
    void gravacaoELeituraEmArquivoPreservamDadosEReferencias() throws Exception {
        Emprestimo e1 = bib.registrarEmprestimo(aluno, livro, D02);
        bib.registrarDevolucao(e1, EstadoMaterial.DANIFICADO, Util.criar(2026, 11, 19));
        Emprestimo e2 = bib.registrarEmprestimo(professor, revista, D02);
        bib.salvarArquivos();

        Biblioteca outra2 = new Biblioteca(bib.getCaminhoArquivo());
        outra2.carregarArquivos();
        assertEquals(4, outra2.getMateriais().size());
        assertEquals(3, outra2.getUsuarios().size());
        assertEquals(2, outra2.getEmprestimos().size());

        Emprestimo lido = outra2.buscarEmprestimo(e2.getCodigo());
        assertTrue(lido.estaEmAberto());
        assertSame(outra2.buscarMaterial("R1"), lido.getMaterial());
        assertSame(outra2.buscarUsuario("123.456.789-00"), lido.getUsuario());
        assertEquals(2, outra2.buscarMaterial("R1").getExemplaresDisponiveis());

        Usuario anaLida = outra2.buscarUsuario("202610001");
        assertEquals(2, anaLida.getPenalidades().size());
        assertTrue(anaLida.temMultaPendente());

        // retoma numeração
        Emprestimo novo = outra2.registrarEmprestimo(outra2.buscarUsuario("987.654.321-00"),
                outra2.buscarMaterial("L1"), Util.criar(2026, 12, 1));
        assertTrue(novo.getCodigo() > e2.getCodigo());
    }

    @Test
    void carregarSemArquivoMantemListasVaziasENaoFalha() throws Exception {
        Biblioteca vazia = new Biblioteca(pasta.resolve("nao-existe.dat").toString());
        vazia.carregarArquivos();
        assertTrue(vazia.getMateriais().isEmpty());
    }

    @Test
    void arquivoCorrompidoLancaExcecaoPropria() throws Exception {
        Path ruim = pasta.resolve("ruim.dat");
        java.nio.file.Files.write(ruim, new byte[] {1, 2, 3, 4, 5});
        Biblioteca b = new Biblioteca(ruim.toString());
        assertErro("erro ao ler", b::carregarArquivos);
    }

    @Test
    void dadosDeExemploSaoValidos() throws Exception {
        Biblioteca b = new Biblioteca(pasta.resolve("ex.dat").toString());
        DadosExemplo.popular(b);
        assertEquals(7, b.getMateriais().size());
        assertEquals(4, b.getUsuarios().size());
    }
}
