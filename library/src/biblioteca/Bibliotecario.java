package biblioteca;

import java.util.ArrayList;
import java.util.Date;

/** operador do sistema */
public class Bibliotecario {
    private final String nome;
    private final String cpf;
    private final Biblioteca biblioteca;

    public Bibliotecario(String nome, String cpf, Biblioteca biblioteca) {
        this.nome = nome == null ? "" : nome.trim();
        this.cpf = cpf == null ? "" : cpf.trim();
        this.biblioteca = biblioteca;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void cadastrarMaterial(Material material) throws BibliotecaException {
        biblioteca.cadastrarMaterial(material);
    }

    public void editarMaterial(Material novo) throws BibliotecaException {
        biblioteca.alterarMaterial(novo);
    }

    public Material consultarMaterial(String codigo) throws BibliotecaException {
        return biblioteca.buscarMaterial(codigo);
    }

    public void removerMaterial(String codigo) throws BibliotecaException {
        biblioteca.removerMaterial(codigo);
    }

    public void cadastrarUsuario(Usuario usuario) throws BibliotecaException {
        biblioteca.cadastrarUsuario(usuario);
    }

    public void editarUsuario(String cpfMatricula, String nome, String telefone, String email)
            throws BibliotecaException {
        biblioteca.alterarUsuario(cpfMatricula, nome, telefone, email);
    }

    public Usuario consultarUsuario(String cpfMatricula) throws BibliotecaException {
        return biblioteca.buscarUsuario(cpfMatricula);
    }

    public void removerUsuario(String cpfMatricula) throws BibliotecaException {
        biblioteca.inativarUsuario(cpfMatricula);
    }

    public Emprestimo registrarEmprestimo(Usuario usuario, Material material) throws BibliotecaException {
        return biblioteca.registrarEmprestimo(usuario, material);
    }

    public Emprestimo registrarEmprestimo(Usuario usuario, Material material, Date data)
            throws BibliotecaException {
        return biblioteca.registrarEmprestimo(usuario, material, data);
    }

    public ArrayList<Penalidade> registrarDevolucao(Emprestimo emprestimo, EstadoMaterial estado)
            throws BibliotecaException {
        return biblioteca.registrarDevolucao(emprestimo, estado);
    }

    public ArrayList<Penalidade> registrarDevolucao(Emprestimo emprestimo, EstadoMaterial estado, Date data)
            throws BibliotecaException {
        return biblioteca.registrarDevolucao(emprestimo, estado, data);
    }

    public ArrayList<Material> consultarAcervo(String texto, String tipo, boolean apenasDisponiveis) {
        return biblioteca.consultarAcervo(texto, tipo, apenasDisponiveis);
    }

    public ArrayList<Emprestimo> consultarEmprestimos(Usuario usuario, Material material, Date inicio, Date fim,
                                                      String situacao, Date referencia) {
        return biblioteca.consultarHistorico(usuario, material, inicio, fim, situacao, referencia);
    }

    @Override
    public String toString() {
        return nome + " (" + cpf + ")";
    }
}
