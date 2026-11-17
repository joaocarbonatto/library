package biblioteca;

import java.util.Date;

public class Multa extends Penalidade {
    private static final long serialVersionUID = 1L;

    private final double valor;
    private final String motivo;
    private boolean quitada;
    private Date dataQuitacao;

    public Multa(double valor, String motivo, Date dataAplicacao) {
        super(dataAplicacao);
        this.valor = Math.round(valor * 100.0) / 100.0;
        this.motivo = motivo;
    }

    public double getValor() {
        return valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public boolean isQuitada() {
        return quitada;
    }

    public Date getDataQuitacao() {
        return dataQuitacao;
    }

    public void quitar() {
        quitar(new Date());
    }

    public void quitar(Date data) {
        this.quitada = true;
        this.dataQuitacao = data;
    }

    @Override
    public boolean estaAtiva(Date referencia) {
        return !quitada;
    }

    @Override
    public String descricao() {
        String situacao = quitada ? "paga em " + Util.formatar(dataQuitacao) : "pendente";
        return "Multa #" + getCodigo() + " de " + Util.moeda(valor) + " (" + motivo + ") - " + situacao;
    }
}
