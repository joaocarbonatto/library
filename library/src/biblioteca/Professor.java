package biblioteca;

public class Professor extends Usuario {
    private static final long serialVersionUID = 1L;

    public Professor(String nome, String cpfMatricula, String telefone, String email) {
        super(nome, cpfMatricula, telefone, email);
    }

    @Override
    public String getTipo() {
        return "Professor";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 5;
    }

    @Override
    public int getPrazoExtra() {
        return 7;
    }
}
