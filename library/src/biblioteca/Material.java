package biblioteca;

import java.io.Serializable;
import java.util.Date;

/** material do acervo (abstrata) */
public abstract class Material implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private String titulo;
    private int ano;
    private int quantidadeExemplares;
    private double valorReposicao;
    private boolean somenteConsulta;
    /** assunto opcional */
    private String assunto = "";
    private int exemplaresEmprestados;

    protected Material(String codigo, String titulo, int ano, int quantidadeExemplares,
                       double valorReposicao, boolean somenteConsulta) {
        this.codigo = codigo == null ? "" : codigo.trim();
        this.titulo = titulo == null ? "" : titulo.trim();
        this.ano = ano;
        this.quantidadeExemplares = quantidadeExemplares;
        this.valorReposicao = valorReposicao;
        this.somenteConsulta = somenteConsulta;
    }

    public String getCodigo() {
        return codigo;
    }

    /** nunca null */
    public String getAssunto() {
        return assunto == null ? "" : assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto == null ? "" : assunto.trim();
    }

    public String getTitulo() {
        return titulo;
    }

    public int getAno() {
        return ano;
    }

    public int getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public double getValorReposicao() {
        return valorReposicao;
    }

    public boolean isSomenteConsulta() {
        return somenteConsulta;
    }

    public int getExemplaresEmprestados() {
        return exemplaresEmprestados;
    }

    public int getExemplaresDisponiveis() {
        return quantidadeExemplares - exemplaresEmprestados;
    }

    public boolean estaDisponivel() {
        return getExemplaresDisponiveis() > 0;
    }

    public boolean podeSerEmprestado() {
        return !somenteConsulta;
    }

    public abstract String getTipo();

    /** prazo em dias */
    public abstract int getPrazoDias();

    /** penalidade por atraso, ou null */
    public abstract Penalidade calcularPenalidade(int diasAtraso, Date dataAplicacao);

    public Penalidade calcularPenalidade(int diasAtraso) {
        return calcularPenalidade(diasAtraso, new Date());
    }

    // controle de exemplares
    void registrarEmprestimo() {
        exemplaresEmprestados++;
    }

    void registrarDevolucao() {
        exemplaresEmprestados--;
    }

    /** extravio: sai do acervo */
    void registrarExtravio() {
        exemplaresEmprestados--;
        quantidadeExemplares--;
    }

    public void validar() throws BibliotecaException {
        if (Util.vazio(codigo)) {
            throw new BibliotecaException("Informe o código do material.");
        }
        if (Util.vazio(titulo)) {
            throw new BibliotecaException("Informe o título do material.");
        }
        int anoAtual = Util.paraLocal(new Date()).getYear();
        if (ano < 1000 || ano > anoAtual + 1) {
            throw new BibliotecaException("Ano inválido: " + ano + ".");
        }
        if (quantidadeExemplares < 1) {
            throw new BibliotecaException("A quantidade de exemplares deve ser pelo menos 1.");
        }
        if (valorReposicao <= 0) {
            throw new BibliotecaException("O valor de reposição deve ser maior que zero.");
        }
    }

    /** copia dados editáveis */
    public void atualizarDe(Material outro) throws BibliotecaException {
        if (outro.quantidadeExemplares < exemplaresEmprestados) {
            throw new BibliotecaException("A quantidade de exemplares (" + outro.quantidadeExemplares
                    + ") não pode ser menor que a de exemplares emprestados (" + exemplaresEmprestados + ").");
        }
        this.titulo = outro.titulo;
        this.ano = outro.ano;
        this.quantidadeExemplares = outro.quantidadeExemplares;
        this.valorReposicao = outro.valorReposicao;
        this.somenteConsulta = outro.somenteConsulta;
        this.assunto = outro.getAssunto();
    }

    protected abstract String detalhesEspecificos();

    public String resumo() {
        return "[" + codigo + "] " + getTipo() + " - " + titulo + " (" + ano + ") | "
                + getExemplaresDisponiveis() + "/" + quantidadeExemplares + " disponível(is)"
                + (somenteConsulta ? " | somente consulta local" : "");
    }

    public String detalhes() {
        return "Código: " + codigo + "\n"
                + "Tipo: " + getTipo() + "\n"
                + "Título: " + titulo + "\n"
                + "Ano: " + ano + "\n"
                + (getAssunto().isEmpty() ? "" : "Assunto: " + getAssunto() + "\n")
                + detalhesEspecificos()
                + "Exemplares: " + quantidadeExemplares + " (disponíveis: " + getExemplaresDisponiveis() + ")\n"
                + "Valor de reposição: " + Util.moeda(valorReposicao) + "\n"
                + "Somente consulta local: " + (somenteConsulta ? "sim" : "não") + "\n"
                + "Prazo de devolução: " + getPrazoDias() + " dias";
    }

    @Override
    public String toString() {
        return resumo();
    }
}
