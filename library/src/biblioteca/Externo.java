package biblioteca;

public class Externo extends Usuario {
    private static final long serialVersionUID = 1L;

    public Externo(String nome, String cpfMatricula, String telefone, String email) {
        super(nome, cpfMatricula, telefone, email);
    }

    @Override
    public String getTipo() {
        return "Externo";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 2;
    }
}
