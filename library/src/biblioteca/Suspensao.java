package biblioteca;

import java.util.Date;

public class Suspensao extends Penalidade {
    private static final long serialVersionUID = 1L;

    private final int dias;
    private final Date dataFim;

    public Suspensao(int dias, Date dataAplicacao) {
        super(dataAplicacao);
        this.dias = dias;
        this.dataFim = Util.somarDias(dataAplicacao, dias);
    }

    public int getDias() {
        return dias;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public boolean estaVigente() {
        return estaVigente(new Date());
    }

    /** vale até dia anterior a dataFim */
    public boolean estaVigente(Date referencia) {
        return Util.paraLocal(referencia).isBefore(Util.paraLocal(dataFim));
    }

    @Override
    public boolean estaAtiva(Date referencia) {
        return estaVigente(referencia);
    }

    @Override
    public String descricao() {
        return "Suspensão #" + getCodigo() + " de " + dias + " dia(s), até " + Util.formatar(dataFim);
    }
}
