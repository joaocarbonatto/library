package biblioteca;

public class Aluno extends Usuario {
    private static final long serialVersionUID = 1L;

    public Aluno(String nome, String cpfMatricula, String telefone, String email) {
        super(nome, cpfMatricula, telefone, email);
    }

    @Override
    public String getTipo() {
        return "Aluno";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 3;
    }
}
