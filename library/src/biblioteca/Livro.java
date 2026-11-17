package biblioteca;

import java.util.Date;

public class Livro extends Material {
    private static final long serialVersionUID = 1L;
    public static final int PRAZO_DIAS = 14;
    public static final double MULTA_POR_DIA = 1.00;

    private String autor;
    private String isbn;

    public Livro(String codigo, String titulo, int ano, int quantidadeExemplares, double valorReposicao,
                 boolean somenteConsulta, String autor, String isbn) {
        super(codigo, titulo, ano, quantidadeExemplares, valorReposicao, somenteConsulta);
        this.autor = autor == null ? "" : autor.trim();
        this.isbn = isbn == null ? "" : isbn.trim();
    }

    public String getAutor() {
        return autor;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public String getTipo() {
        return "Livro";
    }

    @Override
    public int getPrazoDias() {
        return PRAZO_DIAS;
    }

    @Override
    public Penalidade calcularPenalidade(int diasAtraso, Date dataAplicacao) {
        if (diasAtraso <= 0) {
            return null;
        }
        return new Multa(MULTA_POR_DIA * diasAtraso, "atraso de " + diasAtraso + " dia(s) - livro", dataAplicacao);
    }

    @Override
    public void validar() throws BibliotecaException {
        super.validar();
        if (Util.vazio(autor)) {
            throw new BibliotecaException("Informe o autor do livro.");
        }
        if (Util.vazio(isbn)) {
            throw new BibliotecaException("Informe o ISBN do livro.");
        }
    }

    @Override
    public void atualizarDe(Material outro) throws BibliotecaException {
        if (!(outro instanceof Livro)) {
            throw new BibliotecaException("O tipo do material não pode ser alterado.");
        }
        super.atualizarDe(outro);
        this.autor = ((Livro) outro).autor;
        this.isbn = ((Livro) outro).isbn;
    }

    @Override
    protected String detalhesEspecificos() {
        return "Autor: " + autor + "\nISBN: " + isbn + "\n";
    }
}
