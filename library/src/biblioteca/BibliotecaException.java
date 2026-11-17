package biblioteca;

/** exceção própria */
public class BibliotecaException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String mensagem;

    public BibliotecaException(String mensagem) {
        super(mensagem);
        this.mensagem = mensagem;
    }

    @Override
    public String getMessage() {
        return mensagem;
    }
}
