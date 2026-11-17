package biblioteca;

import java.io.Serializable;
import java.util.Date;

/** penalidade (multa ou suspensão); código automático */
public abstract class Penalidade implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int proximoCodigo = 1;

    private final int codigo;
    private final Date dataAplicacao;

    protected Penalidade(Date dataAplicacao) {
        this.codigo = proximoCodigo++;
        this.dataAplicacao = dataAplicacao;
    }

    /** retoma sequência ao carregar */
    static void garantirProximoCodigo(int minimo) {
        if (minimo > proximoCodigo) {
            proximoCodigo = minimo;
        }
    }

    public int getCodigo() {
        return codigo;
    }

    public Date getDataAplicacao() {
        return dataAplicacao;
    }

    public boolean estaAtiva() {
        return estaAtiva(new Date());
    }

    /** ativa: multa não quitada, suspensão vigente */
    public abstract boolean estaAtiva(Date referencia);

    public abstract String descricao();

    @Override
    public String toString() {
        return descricao();
    }
}
