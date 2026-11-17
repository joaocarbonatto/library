package biblioteca;

import java.util.Date;

public class OutraPublicacao extends Material {
    private static final long serialVersionUID = 1L;
    public static final int PRAZO_DIAS = 5;
    public static final int SUSPENSAO_DIAS_POR_DIA_DE_ATRASO = 2;

    private String descricao;

    public OutraPublicacao(String codigo, String titulo, int ano, int quantidadeExemplares, double valorReposicao,
                           boolean somenteConsulta, String descricao) {
        super(codigo, titulo, ano, quantidadeExemplares, valorReposicao, somenteConsulta);
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String getTipo() {
        return "Outra publicação";
    }

    @Override
    public int getPrazoDias() {
        return PRAZO_DIAS;
    }

    /** atraso gera suspensão */
    @Override
    public Penalidade calcularPenalidade(int diasAtraso, Date dataAplicacao) {
        if (diasAtraso <= 0) {
            return null;
        }
        return new Suspensao(SUSPENSAO_DIAS_POR_DIA_DE_ATRASO * diasAtraso, dataAplicacao);
    }

    @Override
    public void validar() throws BibliotecaException {
        super.validar();
        if (Util.vazio(descricao)) {
            throw new BibliotecaException("Informe a descrição da publicação.");
        }
    }

    @Override
    public void atualizarDe(Material outro) throws BibliotecaException {
        if (!(outro instanceof OutraPublicacao)) {
            throw new BibliotecaException("O tipo do material não pode ser alterado.");
        }
        super.atualizarDe(outro);
        this.descricao = ((OutraPublicacao) outro).descricao;
    }

    @Override
    protected String detalhesEspecificos() {
        return "Descrição: " + descricao + "\n";
    }
}
