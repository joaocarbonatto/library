package biblioteca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/** usuário (abstrata) */
public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Pattern PADRAO_IDENTIFICADOR = Pattern.compile("[A-Za-z0-9.\\-/]{3,}");
    private static final Pattern PADRAO_EMAIL = Pattern.compile("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+");
    private static final Pattern PADRAO_TELEFONE = Pattern.compile("[0-9()+\\-\\s]+");

    private String nome;
    private final String cpfMatricula;
    private String telefone;
    private String email;
    private boolean ativo;
    private final ArrayList<Emprestimo> emprestimos = new ArrayList<>();
    private final ArrayList<Penalidade> penalidades = new ArrayList<>();

    protected Usuario(String nome, String cpfMatricula, String telefone, String email) {
        this.nome = nome == null ? "" : nome.trim();
        this.cpfMatricula = cpfMatricula == null ? "" : cpfMatricula.trim();
        this.telefone = telefone == null ? "" : telefone.trim();
        this.email = email == null ? "" : email.trim();
        this.ativo = true;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfMatricula() {
        return cpfMatricula;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public abstract String getTipo();

    public abstract int getLimiteEmprestimos();

    /** dias extras no prazo */
    public int getPrazoExtra() {
        return 0;
    }

    public List<Emprestimo> getEmprestimos() {
        return Collections.unmodifiableList(emprestimos);
    }

    public List<Penalidade> getPenalidades() {
        return Collections.unmodifiableList(penalidades);
    }

    public ArrayList<Material> consultarAcervo(Biblioteca biblioteca, String texto, String tipo,
                                               boolean apenasDisponiveis) {
        return biblioteca.consultarAcervo(texto, tipo, apenasDisponiveis);
    }

    public ArrayList<Emprestimo> consultarEmprestimos(Biblioteca biblioteca, Material material, Date inicio,
                                                      Date fim, String situacao, Date referencia) {
        return biblioteca.consultarHistorico(this, material, inicio, fim, situacao, referencia);
    }

    void adicionarEmprestimo(Emprestimo e) {
        emprestimos.add(e);
    }

    void adicionarPenalidade(Penalidade p) {
        penalidades.add(p);
    }

    public int contarEmprestimosEmAberto() {
        int n = 0;
        for (Emprestimo e : emprestimos) {
            if (e.estaEmAberto()) {
                n++;
            }
        }
        return n;
    }

    public ArrayList<Multa> getMultasPendentes() {
        ArrayList<Multa> lista = new ArrayList<>();
        for (Penalidade p : penalidades) {
            if (p instanceof Multa && !((Multa) p).isQuitada()) {
                lista.add((Multa) p);
            }
        }
        return lista;
    }

    public boolean temMultaPendente() {
        return !getMultasPendentes().isEmpty();
    }

    /** suspensão vigente, ou null */
    public Suspensao getSuspensaoAtiva(Date referencia) {
        for (Penalidade p : penalidades) {
            if (p instanceof Suspensao && ((Suspensao) p).estaVigente(referencia)) {
                return (Suspensao) p;
            }
        }
        return null;
    }

    public boolean temEmprestimoEmAtraso(Date referencia) {
        for (Emprestimo e : emprestimos) {
            if (e.estaEmAtraso(referencia)) {
                return true;
            }
        }
        return false;
    }

    /** motivo de bloqueio, ou null */
    public String motivoBloqueio(Date referencia) {
        if (!ativo) {
            return "o usuário está inativo.";
        }
        if (temMultaPendente()) {
            return "o usuário possui multa pendente.";
        }
        Suspensao s = getSuspensaoAtiva(referencia);
        if (s != null) {
            return "o usuário está suspenso até " + Util.formatar(s.getDataFim()) + ".";
        }
        if (temEmprestimoEmAtraso(referencia)) {
            return "o usuário possui empréstimo em atraso.";
        }
        if (contarEmprestimosEmAberto() >= getLimiteEmprestimos()) {
            return "limite de " + getLimiteEmprestimos() + " empréstimos em aberto atingido (" + getTipo() + ").";
        }
        return null;
    }

    public boolean estaApto() {
        return estaApto(new Date());
    }

    public boolean estaApto(Date referencia) {
        return motivoBloqueio(referencia) == null;
    }

    public String getSituacao(Date referencia) {
        if (!ativo) {
            return "Inativo";
        }
        if (temMultaPendente()) {
            return "Multa pendente";
        }
        if (getSuspensaoAtiva(referencia) != null) {
            return "Suspenso";
        }
        if (temEmprestimoEmAtraso(referencia)) {
            return "Em atraso";
        }
        return "Regular";
    }

    public void validar() throws BibliotecaException {
        validarDados(nome, cpfMatricula, telefone, email);
    }

    private static void validarDados(String nome, String cpfMatricula, String telefone, String email)
            throws BibliotecaException {
        if (Util.vazio(nome)) {
            throw new BibliotecaException("Informe o nome do usuário.");
        }
        if (Util.vazio(cpfMatricula) || !PADRAO_IDENTIFICADOR.matcher(cpfMatricula).matches()) {
            throw new BibliotecaException("CPF ou matrícula inválido(a): use letras, números, pontos e traços (mín. 3 caracteres).");
        }
        if (Util.vazio(telefone) || !PADRAO_TELEFONE.matcher(telefone).matches()
                || telefone.replaceAll("\\D", "").length() < 8) {
            throw new BibliotecaException("Telefone inválido: informe ao menos 8 dígitos.");
        }
        if (Util.vazio(email) || !PADRAO_EMAIL.matcher(email).matches()) {
            throw new BibliotecaException("E-mail inválido.");
        }
    }

    /** altera contato */
    public void atualizarContato(String novoNome, String novoTelefone, String novoEmail) throws BibliotecaException {
        String n = novoNome == null ? "" : novoNome.trim();
        String t = novoTelefone == null ? "" : novoTelefone.trim();
        String e = novoEmail == null ? "" : novoEmail.trim();
        validarDados(n, cpfMatricula, t, e);
        this.nome = n;
        this.telefone = t;
        this.email = e;
    }

    public String resumo(Date referencia) {
        return "[" + cpfMatricula + "] " + nome + " - " + getTipo() + " | " + getSituacao(referencia);
    }

    public String detalhes(Date referencia) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome: ").append(nome).append("\n");
        sb.append("Tipo: ").append(getTipo()).append("\n");
        sb.append("CPF/Matrícula: ").append(cpfMatricula).append("\n");
        sb.append("Telefone: ").append(telefone).append("\n");
        sb.append("E-mail: ").append(email).append("\n");
        sb.append("Situação: ").append(getSituacao(referencia)).append("\n");
        sb.append("Empréstimos em aberto: ").append(contarEmprestimosEmAberto())
                .append(" de ").append(getLimiteEmprestimos()).append("\n");
        if (penalidades.isEmpty()) {
            sb.append("Penalidades: nenhuma");
        } else {
            sb.append("Penalidades:");
            for (Penalidade p : penalidades) {
                sb.append("\n  - ").append(p.descricao());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return resumo(new Date());
    }
}
