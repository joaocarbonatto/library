package biblioteca;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** interface gráfica (JOptionPane) */
public class InterfaceBiblioteca {
    private static final String TITULO = "Sistema de Biblioteca";
    private static final String[] TIPOS_MATERIAL = {"Livro", "Revista", "Outra publicação"};
    private static final String[] TIPOS_USUARIO = {"Aluno", "Professor", "Externo"};

    private interface Acao {
        void executar() throws BibliotecaException;
    }

    private final Biblioteca biblioteca;
    private Bibliotecario bibliotecario;

    public InterfaceBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    // ================================================================== Fluxo principal

    public void iniciar() {
        executar(this::carregarDados);
        boolean sair = false;
        while (!sair) {
            String perfil = escolher("Selecione o perfil de acesso:", "Acesso", "Bibliotecário", "Usuário", "Sair");
            if (perfil == null || perfil.equals("Sair")) {
                sair = true;
            } else if (perfil.equals("Bibliotecário")) {
                if (identificarBibliotecario()) {
                    menuBibliotecario();
                }
            } else {
                executar(this::menuUsuario);
            }
        }
        executar(biblioteca::salvarArquivos);
        informar("Dados salvos. Até logo!");
    }

    private void carregarDados() throws BibliotecaException {
        biblioteca.carregarArquivos();
        if (biblioteca.getMateriais().isEmpty() && biblioteca.getUsuarios().isEmpty()) {
            int r = JOptionPane.showConfirmDialog(null,
                    "Nenhum dado encontrado.\nDeseja carregar dados de exemplo para demonstração?",
                    TITULO, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                DadosExemplo.popular(biblioteca);
                biblioteca.salvarArquivos();
            }
        }
    }

    private boolean identificarBibliotecario() {
        if (bibliotecario != null) {
            return true;
        }
        while (true) {
            String[] v = formulario("Identificação do bibliotecário", null,
                    new String[] {"Nome", "CPF"}, new String[] {"", ""}, null);
            if (v == null) {
                return false;
            }
            if (Util.vazio(v[0]) || Util.vazio(v[1])) {
                erro("Informe o nome e o CPF.");
            } else {
                bibliotecario = new Bibliotecario(v[0], v[1], biblioteca);
                return true;
            }
        }
    }

    // ================================================================== Menus

    private void menuBibliotecario() {
        while (true) {
            String op = escolher("Bibliotecário: " + bibliotecario.getNome() + "\nEscolha uma opção:", "Menu do bibliotecário",
                    "Materiais", "Usuários", "Registrar empréstimo", "Registrar devolução",
                    "Registrar quitação de multa", "Consultar acervo", "Consultar histórico de empréstimos",
                    "Salvar dados", "Voltar");
            if (op == null || op.equals("Voltar")) {
                return;
            }
            switch (op) {
                case "Materiais":
                    menuMateriais();
                    break;
                case "Usuários":
                    menuUsuarios();
                    break;
                case "Registrar empréstimo":
                    executar(this::registrarEmprestimo);
                    break;
                case "Registrar devolução":
                    executar(this::registrarDevolucao);
                    break;
                case "Registrar quitação de multa":
                    executar(this::registrarQuitacao);
                    break;
                case "Consultar acervo":
                    executar(() -> consultarAcervo(null));
                    break;
                case "Consultar histórico de empréstimos":
                    executar(() -> consultarHistorico(null));
                    break;
                case "Salvar dados":
                    executar(() -> {
                        biblioteca.salvarArquivos();
                        informar("Dados salvos em " + biblioteca.getCaminhoArquivo());
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private void menuMateriais() {
        while (true) {
            String op = escolher("Materiais - escolha a operação:", "Materiais",
                    "Cadastrar", "Alterar", "Consultar", "Listar", "Remover", "Voltar");
            if (op == null || op.equals("Voltar")) {
                return;
            }
            switch (op) {
                case "Cadastrar":
                    executar(this::cadastrarMaterial);
                    break;
                case "Alterar":
                    executar(this::alterarMaterial);
                    break;
                case "Consultar":
                    executar(this::consultarMaterial);
                    break;
                case "Listar":
                    executar(this::listarMateriais);
                    break;
                case "Remover":
                    executar(this::removerMaterial);
                    break;
                default:
                    break;
            }
        }
    }

    private void menuUsuarios() {
        while (true) {
            String op = escolher("Usuários - escolha a operação:", "Usuários",
                    "Cadastrar", "Alterar", "Consultar", "Listar", "Inativar", "Reativar", "Voltar");
            if (op == null || op.equals("Voltar")) {
                return;
            }
            switch (op) {
                case "Cadastrar":
                    executar(this::cadastrarUsuario);
                    break;
                case "Alterar":
                    executar(this::alterarUsuario);
                    break;
                case "Consultar":
                    executar(this::consultarUsuario);
                    break;
                case "Listar":
                    executar(this::listarUsuarios);
                    break;
                case "Inativar":
                    executar(this::inativarUsuario);
                    break;
                case "Reativar":
                    executar(this::reativarUsuario);
                    break;
                default:
                    break;
            }
        }
    }

    private void menuUsuario() throws BibliotecaException {
        String id = entrada("Informe seu CPF ou matrícula:");
        if (id == null) {
            return;
        }
        Usuario u = biblioteca.buscarUsuario(id);
        while (true) {
            String op = escolher("Olá, " + u.getNome() + "!\nEscolha uma opção:", "Menu do usuário",
                    "Consultar acervo", "Meu histórico de empréstimos", "Minha situação", "Voltar");
            if (op == null || op.equals("Voltar")) {
                return;
            }
            final Usuario logado = u;
            switch (op) {
                case "Consultar acervo":
                    executar(() -> consultarAcervo(logado));
                    break;
                case "Meu histórico de empréstimos":
                    executar(() -> consultarHistorico(logado));
                    break;
                case "Minha situação":
                    mostrarTexto("Minha situação", logado.detalhes(new Date()));
                    break;
                default:
                    break;
            }
        }
    }

    // ================================================================== Materiais

    private void cadastrarMaterial() throws BibliotecaException {
        String tipo = escolher("Tipo de material:", "Cadastrar material", TIPOS_MATERIAL);
        if (tipo == null) {
            return;
        }
        String[] rotulos = rotulosMaterial(tipo, true);
        String[] valores = new String[rotulos.length];
        java.util.Arrays.fill(valores, "");
        Material criado = null;
        while (criado == null) {
            valores = formulario("Cadastrar " + tipo.toLowerCase(), null, rotulos, valores, null);
            if (valores == null) {
                return;
            }
            try {
                Material m = montarMaterial(tipo, valores);
                bibliotecario.cadastrarMaterial(m);
                criado = m;
            } catch (BibliotecaException e) {
                erro(e.getMessage());
            }
        }
        biblioteca.salvarArquivos();
        informar("Material cadastrado: " + criado.getTitulo());
    }

    private void alterarMaterial() throws BibliotecaException {
        String codigo = entrada("Código do material a alterar:");
        if (codigo == null) {
            return;
        }
        Material atual = bibliotecario.consultarMaterial(codigo);
        String[] rotulos = rotulosMaterial(atual.getTipo(), false);
        String[] valores = valoresMaterial(atual);
        boolean alterado = false;
        while (!alterado) {
            valores = formulario("Alterar " + atual.getTipo().toLowerCase() + " [" + atual.getCodigo() + "]",
                    null, rotulos, valores, null);
            if (valores == null) {
                return;
            }
            try {
                String[] completos = new String[valores.length + 1];
                completos[0] = atual.getCodigo();
                System.arraycopy(valores, 0, completos, 1, valores.length);
                bibliotecario.editarMaterial(montarMaterial(atual.getTipo(), completos));
                alterado = true;
            } catch (BibliotecaException e) {
                erro(e.getMessage());
            }
        }
        biblioteca.salvarArquivos();
        informar("Material alterado com sucesso.");
    }

    private void consultarMaterial() throws BibliotecaException {
        String codigo = entrada("Código do material:");
        if (codigo != null) {
            mostrarTexto("Material", bibliotecario.consultarMaterial(codigo).detalhes());
        }
    }

    private void listarMateriais() {
        ArrayList<Material> lista = biblioteca.listarMateriais();
        if (lista.isEmpty()) {
            informar("Nenhum material cadastrado.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Material m : lista) {
            sb.append(m.resumo()).append("\n");
        }
        mostrarTexto("Materiais cadastrados (" + lista.size() + ")", sb.toString());
    }

    private void removerMaterial() throws BibliotecaException {
        String codigo = entrada("Código do material a remover:");
        if (codigo == null) {
            return;
        }
        Material m = bibliotecario.consultarMaterial(codigo);
        if (confirmar("Remover o material \"" + m.getTitulo() + "\"?")) {
            bibliotecario.removerMaterial(codigo);
            biblioteca.salvarArquivos();
            informar("Material removido.");
        }
    }

    private String[] rotulosMaterial(String tipo, boolean comCodigo) {
        ArrayList<String> r = new ArrayList<>();
        if (comCodigo) {
            r.add("Código");
        }
        r.add("Título");
        r.add("Ano");
        r.add("Quantidade de exemplares");
        r.add("Valor de reposição (R$)");
        r.add("Somente consulta local? (S/N)");
        r.add("Assunto (opcional)");
        if (tipo.equals("Livro")) {
            r.add("Autor");
            r.add("ISBN");
        } else if (tipo.equals("Revista")) {
            r.add("Edição");
            r.add("Periodicidade");
        } else {
            r.add("Descrição");
        }
        return r.toArray(new String[0]);
    }

    /** valores atuais para alteração */
    private String[] valoresMaterial(Material m) {
        ArrayList<String> v = new ArrayList<>();
        v.add(m.getTitulo());
        v.add(String.valueOf(m.getAno()));
        v.add(String.valueOf(m.getQuantidadeExemplares()));
        v.add(String.valueOf(m.getValorReposicao()));
        v.add(m.isSomenteConsulta() ? "S" : "N");
        v.add(m.getAssunto());
        if (m instanceof Livro) {
            v.add(((Livro) m).getAutor());
            v.add(((Livro) m).getIsbn());
        } else if (m instanceof Revista) {
            v.add(String.valueOf(((Revista) m).getEdicao()));
            v.add(((Revista) m).getPeriodicidade());
        } else {
            v.add(((OutraPublicacao) m).getDescricao());
        }
        return v.toArray(new String[0]);
    }

    /** v: código, título, ano, quantidade, valor, consulta local, assunto, específicos */
    private Material montarMaterial(String tipo, String[] v) throws BibliotecaException {
        int ano = inteiro("Ano", v[2]);
        int qtd = inteiro("Quantidade de exemplares", v[3]);
        double valor = decimal("Valor de reposição", v[4]);
        boolean consulta = simNao("Somente consulta local", v[5]);
        Material m;
        if (tipo.equals("Livro")) {
            m = new Livro(v[0], v[1], ano, qtd, valor, consulta, v[7], v[8]);
        } else if (tipo.equals("Revista")) {
            m = new Revista(v[0], v[1], ano, qtd, valor, consulta, inteiro("Edição", v[7]), v[8]);
        } else {
            m = new OutraPublicacao(v[0], v[1], ano, qtd, valor, consulta, v[7]);
        }
        m.setAssunto(v[6]);
        return m;
    }

    // ================================================================== Usuários

    private void cadastrarUsuario() throws BibliotecaException {
        String tipo = escolher("Tipo de usuário:", "Cadastrar usuário", TIPOS_USUARIO);
        if (tipo == null) {
            return;
        }
        String[] rotulos = {"Nome", "CPF ou matrícula", "Telefone", "E-mail"};
        String[] valores = {"", "", "", ""};
        Usuario criado = null;
        while (criado == null) {
            valores = formulario("Cadastrar " + tipo.toLowerCase(), null, rotulos, valores, null);
            if (valores == null) {
                return;
            }
            Usuario u;
            if (tipo.equals("Aluno")) {
                u = new Aluno(valores[0], valores[1], valores[2], valores[3]);
            } else if (tipo.equals("Professor")) {
                u = new Professor(valores[0], valores[1], valores[2], valores[3]);
            } else {
                u = new Externo(valores[0], valores[1], valores[2], valores[3]);
            }
            try {
                bibliotecario.cadastrarUsuario(u);
                criado = u;
            } catch (BibliotecaException e) {
                erro(e.getMessage());
            }
        }
        biblioteca.salvarArquivos();
        informar("Usuário cadastrado: " + criado.getNome());
    }

    private void alterarUsuario() throws BibliotecaException {
        String id = entrada("CPF ou matrícula do usuário a alterar:");
        if (id == null) {
            return;
        }
        Usuario u = biblioteca.buscarUsuario(id);
        String[] rotulos = {"Nome", "Telefone", "E-mail"};
        String[] valores = {u.getNome(), u.getTelefone(), u.getEmail()};
        boolean alterado = false;
        while (!alterado) {
            valores = formulario("Alterar usuário [" + u.getCpfMatricula() + "] (CPF/matrícula e tipo não mudam)",
                    null, rotulos, valores, null);
            if (valores == null) {
                return;
            }
            try {
                bibliotecario.editarUsuario(u.getCpfMatricula(), valores[0], valores[1], valores[2]);
                alterado = true;
            } catch (BibliotecaException e) {
                erro(e.getMessage());
            }
        }
        biblioteca.salvarArquivos();
        informar("Usuário alterado com sucesso.");
    }

    private void consultarUsuario() throws BibliotecaException {
        String id = entrada("CPF ou matrícula do usuário:");
        if (id != null) {
            mostrarTexto("Usuário", bibliotecario.consultarUsuario(id).detalhes(new Date()));
        }
    }

    private void listarUsuarios() {
        ArrayList<Usuario> lista = biblioteca.listarUsuarios();
        if (lista.isEmpty()) {
            informar("Nenhum usuário cadastrado.");
            return;
        }
        Date hoje = new Date();
        StringBuilder sb = new StringBuilder();
        for (Usuario u : lista) {
            sb.append(u.resumo(hoje)).append("\n");
        }
        mostrarTexto("Usuários cadastrados (" + lista.size() + ")", sb.toString());
    }

    private void inativarUsuario() throws BibliotecaException {
        String id = entrada("CPF ou matrícula do usuário a inativar:");
        if (id == null) {
            return;
        }
        Usuario u = biblioteca.buscarUsuario(id);
        if (confirmar("Inativar o usuário \"" + u.getNome() + "\"?")) {
            bibliotecario.removerUsuario(id);
            biblioteca.salvarArquivos();
            informar("Usuário inativado.");
        }
    }

    private void reativarUsuario() throws BibliotecaException {
        String id = entrada("CPF ou matrícula do usuário a reativar:");
        if (id != null) {
            biblioteca.reativarUsuario(id);
            biblioteca.salvarArquivos();
            informar("Usuário reativado.");
        }
    }

    // ================================================================== Empréstimos, devoluções e multas

    private void registrarEmprestimo() throws BibliotecaException {
        String[] v = formulario("Registrar empréstimo", null,
                new String[] {"CPF ou matrícula do usuário", "Código do material",
                        "Data do empréstimo (dd/MM/aaaa; vazio = hoje)"},
                new String[] {"", "", ""}, null);
        if (v == null) {
            return;
        }
        Date data = dataOpcional(v[2], "empréstimo");
        Usuario u = bibliotecario.consultarUsuario(v[0]);
        Material m = bibliotecario.consultarMaterial(v[1]);
        Emprestimo e = bibliotecario.registrarEmprestimo(u, m, data);
        biblioteca.salvarArquivos();
        mostrarTexto("Comprovante de empréstimo", e.comprovante());
    }

    private void registrarDevolucao() throws BibliotecaException {
        ArrayList<Emprestimo> abertos = biblioteca.listarEmprestimosEmAberto();
        if (abertos.isEmpty()) {
            informar("Não há empréstimos em aberto.");
            return;
        }
        Date hoje = new Date();
        StringBuilder sb = new StringBuilder("Empréstimos em aberto:\n");
        for (Emprestimo e : abertos) {
            sb.append(e.resumo(hoje)).append("\n");
        }
        String[] v = formulario("Registrar devolução", sb.toString(),
                new String[] {"Código do empréstimo", "Estado do material",
                        "Data da devolução (dd/MM/aaaa; vazio = hoje)"},
                new String[] {"", EstadoMaterial.BOM.getRotulo(), ""},
                new String[][] {null,
                        {EstadoMaterial.BOM.getRotulo(), EstadoMaterial.DANIFICADO.getRotulo(),
                                EstadoMaterial.EXTRAVIADO.getRotulo()},
                        null});
        if (v == null) {
            return;
        }
        Emprestimo emp = biblioteca.buscarEmprestimo(inteiro("Código do empréstimo", v[0]));
        EstadoMaterial estado = EstadoMaterial.deRotulo(v[1]);
        Date data = dataOpcional(v[2], "devolução");
        ArrayList<Penalidade> penalidades = bibliotecario.registrarDevolucao(emp, estado, data);
        biblioteca.salvarArquivos();

        StringBuilder r = new StringBuilder();
        r.append("Devolução registrada.\n");
        r.append("Material: ").append(emp.getMaterial().getTitulo()).append("\n");
        r.append("Usuário: ").append(emp.getUsuario().getNome()).append("\n");
        r.append("Devolução prevista: ").append(Util.formatar(emp.getDataPrevista())).append("\n");
        r.append("Dias de atraso: ").append(emp.calcularAtraso()).append("\n");
        if (penalidades.isEmpty()) {
            r.append("Nenhuma penalidade aplicada.");
        } else {
            r.append("Penalidades aplicadas:");
            for (Penalidade p : penalidades) {
                r.append("\n  - ").append(p.descricao());
            }
        }
        mostrarTexto("Devolução", r.toString());
    }

    private void registrarQuitacao() throws BibliotecaException {
        String id = entrada("CPF ou matrícula do usuário:");
        if (id == null) {
            return;
        }
        Usuario u = biblioteca.buscarUsuario(id);
        ArrayList<Multa> multas = u.getMultasPendentes();
        if (multas.isEmpty()) {
            throw new BibliotecaException("O usuário não possui multa pendente: não há nada a quitar.");
        }
        StringBuilder sb = new StringBuilder("Multas pendentes de " + u.getNome() + ":\n");
        for (Multa m : multas) {
            sb.append(m.descricao()).append("\n");
        }
        String[] v = formulario("Registrar quitação de multa", sb.toString(),
                new String[] {"Código da multa paga"}, new String[] {""}, null);
        if (v == null) {
            return;
        }
        biblioteca.registrarQuitacao(u, inteiro("Código da multa", v[0]));
        biblioteca.salvarArquivos();
        informar("Quitação registrada.\nSituação atual do usuário: " + u.getSituacao(new Date()));
    }

    // ================================================================== Consultas

    private void consultarAcervo(Usuario logado) throws BibliotecaException {
        String[] v = formulario("Consultar acervo", null,
                new String[] {"Título, autor, assunto ou código (vazio = todos)", "Tipo", "Apenas disponíveis?"},
                new String[] {"", "Todos", "Não"},
                new String[][] {null, {"Todos", "Livro", "Revista", "Outra publicação"}, {"Não", "Sim"}});
        if (v == null) {
            return;
        }
        boolean apenas = v[2].equals("Sim");
        ArrayList<Material> achados = logado == null
                ? bibliotecario.consultarAcervo(v[0], v[1], apenas)
                : logado.consultarAcervo(biblioteca, v[0], v[1], apenas);
        if (achados.isEmpty()) {
            informar("Nenhum material encontrado. Tente alterar os critérios de busca.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Material m : achados) {
            sb.append(m.resumo());
            if (!m.estaDisponivel()) {
                Date prevista = biblioteca.getPrevisaoDevolucao(m);
                if (prevista != null) {
                    sb.append(" | emprestado, devolução prevista: ").append(Util.formatar(prevista));
                }
            }
            sb.append("\n");
        }
        mostrarTexto("Acervo (" + achados.size() + " resultado(s))", sb.toString());
        String codigo = entrada("Para ver os detalhes, informe o código de um material (ou cancele):");
        if (codigo != null && !Util.vazio(codigo)) {
            mostrarTexto("Material", biblioteca.buscarMaterial(codigo).detalhes());
        }
    }

    /** usuarioFixo: só próprio histórico */
    private void consultarHistorico(Usuario usuarioFixo) throws BibliotecaException {
        String[] rotulos;
        String[] iniciais;
        String[][] opcoes;
        String[] situacoes = {"Todas", "Em aberto", "Em atraso", "Devolvido", "Extraviado"};
        if (usuarioFixo == null) {
            rotulos = new String[] {"CPF ou matrícula (opcional)", "Código do material (opcional)",
                    "Período - início (dd/MM/aaaa, opcional)", "Período - fim (dd/MM/aaaa, opcional)", "Situação"};
            iniciais = new String[] {"", "", "", "", "Todas"};
            opcoes = new String[][] {null, null, null, null, situacoes};
        } else {
            rotulos = new String[] {"Período - início (dd/MM/aaaa, opcional)", "Período - fim (dd/MM/aaaa, opcional)",
                    "Situação"};
            iniciais = new String[] {"", "", "Todas"};
            opcoes = new String[][] {null, null, situacoes};
        }
        String[] v = formulario("Histórico de empréstimos", null, rotulos, iniciais, opcoes);
        if (v == null) {
            return;
        }
        Usuario usuario = usuarioFixo;
        Material material = null;
        int i = 0;
        if (usuarioFixo == null) {
            if (!Util.vazio(v[0])) {
                usuario = biblioteca.buscarUsuario(v[0]);
            }
            if (!Util.vazio(v[1])) {
                material = biblioteca.buscarMaterial(v[1]);
            }
            i = 2;
        }
        Date inicio = Util.vazio(v[i]) ? null : Util.interpretar(v[i]);
        Date fim = Util.vazio(v[i + 1]) ? null : Util.interpretar(v[i + 1]);
        Date hoje = new Date();
        ArrayList<Emprestimo> lista = usuarioFixo == null
                ? bibliotecario.consultarEmprestimos(usuario, material, inicio, fim, v[i + 2], hoje)
                : usuarioFixo.consultarEmprestimos(biblioteca, material, inicio, fim, v[i + 2], hoje);
        if (lista.isEmpty()) {
            informar("Não há empréstimos registrados para os critérios informados.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Emprestimo e : lista) {
            sb.append(e.resumo(hoje)).append("\n");
        }
        mostrarTexto("Histórico (" + lista.size() + " empréstimo(s))", sb.toString());
    }

    // ================================================================== Conversões

    private int inteiro(String campo, String texto) throws BibliotecaException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new BibliotecaException("Valor inválido no campo \"" + campo + "\": informe um número inteiro.");
        }
    }

    private double decimal(String campo, String texto) throws BibliotecaException {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new BibliotecaException("Valor inválido no campo \"" + campo + "\": informe um número (ex.: 49,90).");
        }
    }

    private boolean simNao(String campo, String texto) throws BibliotecaException {
        String t = Util.normalizar(texto);
        if (t.equals("s") || t.equals("sim")) {
            return true;
        }
        if (t.isEmpty() || t.equals("n") || t.equals("nao")) {
            return false;
        }
        throw new BibliotecaException("Valor inválido no campo \"" + campo + "\": responda S ou N.");
    }

    /** data dd/MM/aaaa; vazio = hoje; sem futuro */
    private Date dataOpcional(String texto, String nome) throws BibliotecaException {
        if (Util.vazio(texto)) {
            return new Date();
        }
        Date d = Util.interpretar(texto);
        if (Util.paraLocal(d).isAfter(LocalDate.now())) {
            throw new BibliotecaException("A data da " + nome + " não pode ser futura.");
        }
        return d;
    }

    // ================================================================== Caixas de diálogo

    private void executar(Acao acao) {
        try {
            acao.executar();
        } catch (BibliotecaException e) {
            erro(e.getMessage());
        } catch (RuntimeException e) {
            erro("Erro inesperado: " + e);
        }
    }

    private String escolher(String mensagem, String titulo, String... opcoes) {
        Object r = JOptionPane.showInputDialog(null, mensagem, titulo, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
        return r == null ? null : r.toString();
    }

    private String entrada(String mensagem) {
        String r = JOptionPane.showInputDialog(null, mensagem, TITULO, JOptionPane.QUESTION_MESSAGE);
        return r == null ? null : r.trim();
    }

    private boolean confirmar(String mensagem) {
        return JOptionPane.showConfirmDialog(null, mensagem, TITULO, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void informar(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, TITULO + " - Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarTexto(String titulo, String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane rolagem = new JScrollPane(area);
        rolagem.setPreferredSize(new Dimension(820, 360));
        JOptionPane.showMessageDialog(null, rolagem, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    /** formulário; opcoes[i] = lista; null se cancelar */
    private String[] formulario(String titulo, String cabecalho, String[] rotulos, String[] iniciais,
                                String[][] opcoes) {
        JPanel campos = new JPanel(new GridLayout(rotulos.length, 2, 8, 6));
        JTextField[] textos = new JTextField[rotulos.length];
        JComboBox<?>[] listas = new JComboBox<?>[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            campos.add(new JLabel(rotulos[i]));
            String inicial = iniciais != null && iniciais[i] != null ? iniciais[i] : "";
            if (opcoes != null && opcoes[i] != null) {
                listas[i] = new JComboBox<>(opcoes[i]);
                listas[i].setSelectedItem(inicial);
                campos.add(listas[i]);
            } else {
                textos[i] = new JTextField(inicial, 22);
                campos.add(textos[i]);
            }
        }
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        if (cabecalho != null) {
            JTextArea area = new JTextArea(cabecalho);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane rolagem = new JScrollPane(area);
            rolagem.setPreferredSize(new Dimension(780, 200));
            painel.add(rolagem, BorderLayout.NORTH);
        }
        painel.add(campos, BorderLayout.CENTER);

        int r = JOptionPane.showConfirmDialog(null, painel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] valores = new String[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            valores[i] = listas[i] != null ? String.valueOf(listas[i].getSelectedItem()) : textos[i].getText().trim();
        }
        return valores;
    }
}
