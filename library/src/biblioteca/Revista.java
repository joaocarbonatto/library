package biblioteca;

import java.util.Date;

public class Revista extends Material {
    private static final long serialVersionUID = 1L;
    public static final int PRAZO_DIAS = 7;
    public static final double MULTA_POR_DIA = 2.00;

    private int edicao;
    private String periodicidade;

    public Revista(String codigo, String titulo, int ano, int quantidadeExemplares, double valorReposicao,
                   boolean somenteConsulta, int edicao, String periodicidade) {
        super(codigo, titulo, ano, quantidadeExemplares, valorReposicao, somenteConsulta);
        this.edicao = edicao;
        this.periodicidade = periodicidade == null ? "" : periodicidade.trim();
    }

    public int getEdicao() {
        return edicao;
    }

    public String getPeriodicidade() {
        return periodicidade;
    }

    @Override
    public String getTipo() {
        return "Revista";
    }

    @Override
    public int getPrazoDias() {
        return PRAZO_DIAS;
    }

    @Override
    public Penalidade calcularPenalidade(int diasAtraso, Date dataAplicacao) {
        if (diasAtraso <= 0) {
            return null;
        }
        return new Multa(MULTA_POR_DIA * diasAtraso, "atraso de " + diasAtraso + " dia(s) - revista", dataAplicacao);
    }

    @Override
    public void validar() throws BibliotecaException {
        super.validar();
        if (edicao < 1) {
            throw new BibliotecaException("A edição da revista deve ser um número positivo.");
        }
        if (Util.vazio(periodicidade)) {
            throw new BibliotecaException("Informe a periodicidade da revista.");
        }
    }

    @Override
    public void atualizarDe(Material outro) throws BibliotecaException {
        if (!(outro instanceof Revista)) {
            throw new BibliotecaException("O tipo do material não pode ser alterado.");
        }
        super.atualizarDe(outro);
        this.edicao = ((Revista) outro).edicao;
        this.periodicidade = ((Revista) outro).periodicidade;
    }

    @Override
    protected String detalhesEspecificos() {
        return "Edição: " + edicao + "\nPeriodicidade: " + periodicidade + "\n";
    }
}
