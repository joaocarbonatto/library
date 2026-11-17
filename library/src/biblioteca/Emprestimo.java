package biblioteca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** empréstimo; código automático (estático) */
public class Emprestimo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EM_ABERTO = "Em aberto";
    public static final String DEVOLVIDO = "Devolvido";
    public static final String EXTRAVIADO = "Extraviado";
    public static final String EM_ATRASO = "Em atraso";

    private static int proximoCodigo = 1;

    private final int codigo;
    private final Usuario usuario;
    private final Material material;
    private final Date dataEmprestimo;
    private final Date dataPrevista;
    private Date dataDevolucao;
    private String situacao;
    private EstadoMaterial estadoDevolucao;
    private final ArrayList<Penalidade> penalidades = new ArrayList<>();

    public Emprestimo(Usuario usuario, Material material) {
        this(usuario, material, new Date());
    }

    public Emprestimo(Usuario usuario, Material material, Date dataEmprestimo) {
        this.codigo = proximoCodigo++;
        this.usuario = usuario;
        this.material = material;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = Util.somarDias(dataEmprestimo, material.getPrazoDias() + usuario.getPrazoExtra());
        this.situacao = EM_ABERTO;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public Material getMaterial() {
        return material;
    }

    public Date getDataEmprestimo() {
        return dataEmprestimo;
    }

    public Date getDataPrevista() {
        return dataPrevista;
    }

    public Date getDataDevolucao() {
        return dataDevolucao;
    }

    public String getSituacao() {
        return situacao;
    }

    public EstadoMaterial getEstadoDevolucao() {
        return estadoDevolucao;
    }

    public List<Penalidade> getPenalidades() {
        return Collections.unmodifiableList(penalidades);
    }

    public boolean estaEmAberto() {
        return EM_ABERTO.equals(situacao);
    }

    /** dias de atraso */
    public int calcularAtraso() {
        return calcularAtraso(dataDevolucao != null ? dataDevolucao : new Date());
    }

    public int calcularAtraso(Date referencia) {
        long dias = Util.diasEntre(dataPrevista, referencia);
        return dias > 0 ? (int) dias : 0;
    }

    public boolean estaEmAtraso() {
        return estaEmAtraso(new Date());
    }

    public boolean estaEmAtraso(Date referencia) {
        return estaEmAberto() && calcularAtraso(referencia) > 0;
    }

    /** situação para exibição */
    public String getSituacaoExibicao(Date referencia) {
        return estaEmAtraso(referencia) ? EM_ATRASO : situacao;
    }

    public ArrayList<Penalidade> encerrar(EstadoMaterial estado) throws BibliotecaException {
        return encerrar(estado, new Date());
    }

    /** encerra, gera penalidades */
    public ArrayList<Penalidade> encerrar(EstadoMaterial estado, Date data) throws BibliotecaException {
        if (!estaEmAberto()) {
            throw new BibliotecaException("O empréstimo " + codigo + " já foi encerrado.");
        }
        if (estado == null) {
            throw new BibliotecaException("Informe o estado do material.");
        }
        if (Util.diasEntre(dataEmprestimo, data) < 0) {
            throw new BibliotecaException("A data de devolução não pode ser anterior à do empréstimo.");
        }
        ArrayList<Penalidade> geradas = new ArrayList<>();
        if (estado == EstadoMaterial.EXTRAVIADO) {
            geradas.add(new Multa(material.getValorReposicao(), "extravio do material", data));
        } else {
            Penalidade atraso = material.calcularPenalidade(calcularAtraso(data), data);
            if (atraso != null) {
                geradas.add(atraso);
            }
            if (estado == EstadoMaterial.DANIFICADO) {
                geradas.add(new Multa(material.getValorReposicao() * 0.5, "material danificado", data));
            }
        }
        this.dataDevolucao = data;
        this.estadoDevolucao = estado;
        this.situacao = estado == EstadoMaterial.EXTRAVIADO ? EXTRAVIADO : DEVOLVIDO;
        for (Penalidade p : geradas) {
            penalidades.add(p);
            usuario.adicionarPenalidade(p);
        }
        return geradas;
    }

    public String comprovante() {
        return "Empréstimo nº " + codigo + "\n"
                + "Usuário: " + usuario.getNome() + " (" + usuario.getTipo() + ")\n"
                + "Material: " + material.getTitulo() + " [" + material.getCodigo() + "]\n"
                + "Data do empréstimo: " + Util.formatar(dataEmprestimo) + "\n"
                + "Devolução prevista: " + Util.formatar(dataPrevista);
    }

    public String resumo(Date referencia) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(codigo).append(" | ").append(material.getTitulo())
                .append(" | ").append(usuario.getNome())
                .append(" | emp. ").append(Util.formatar(dataEmprestimo))
                .append(" | prev. ").append(Util.formatar(dataPrevista))
                .append(" | dev. ").append(Util.formatar(dataDevolucao))
                .append(" | ").append(getSituacaoExibicao(referencia));
        for (Penalidade p : penalidades) {
            sb.append("\n      ").append(p.descricao());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return resumo(new Date());
    }
}
