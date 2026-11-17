package biblioteca;

/** dados de exemplo */
public final class DadosExemplo {
    private DadosExemplo() {
    }

    public static void popular(Biblioteca b) throws BibliotecaException {
        b.cadastrarMaterial(new Livro("L001", "Estruturas de Dados em Java", 2019, 3, 89.90, false, "Robert Lafore", "9788535236996"));
        b.cadastrarMaterial(new Livro("L002", "Dom Casmurro", 1899, 2, 45.00, false, "Machado de Assis", "9788535910663"));
        b.cadastrarMaterial(new Livro("L003", "Java: Como Programar", 2016, 1, 150.00, false, "Paul Deitel", "9788543004792"));
        b.buscarMaterialOuNull("L001").setAssunto("Programação");
        b.buscarMaterialOuNull("L002").setAssunto("Literatura brasileira");
        b.buscarMaterialOuNull("L003").setAssunto("Programação");
        b.cadastrarMaterial(new Revista("R001", "Superinteressante", 2026, 2, 25.00, false, 450, "Mensal"));
        b.cadastrarMaterial(new Revista("R002", "Revista Piauí", 2026, 1, 30.00, false, 220, "Mensal"));
        b.cadastrarMaterial(new OutraPublicacao("O001", "Jornal O Popular - Edição especial", 2026, 2, 12.00, false, "Jornal impresso"));
        b.cadastrarMaterial(new OutraPublicacao("O002", "Atlas Geográfico Escolar", 2020, 1, 80.00, true, "Atlas (consulta local)"));

        b.cadastrarUsuario(new Aluno("Ana Souza", "202610001", "(62) 99999-0001", "ana.souza@exemplo.com"));
        b.cadastrarUsuario(new Aluno("Bruno Lima", "202610002", "(62) 99999-0002", "bruno.lima@exemplo.com"));
        b.cadastrarUsuario(new Professor("Carlos Mendes", "123.456.789-00", "(62) 98888-0003", "carlos.mendes@exemplo.com"));
        b.cadastrarUsuario(new Externo("Marina Alves", "987.654.321-00", "(62) 97777-0004", "marina.alves@exemplo.com"));
    }
}
