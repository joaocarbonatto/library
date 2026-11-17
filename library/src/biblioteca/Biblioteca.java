package biblioteca;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** núcleo: listas, regras, arquivo */
public class Biblioteca {
    public static final String ARQUIVO_PADRAO = "dados/biblioteca.dat";

    private final ArrayList<Material> materiais = new ArrayList<>();
    private final ArrayList<Usuario> usuarios = new ArrayList<>();
    private final ArrayList<Emprestimo> emprestimos = new ArrayList<>();
    private final String caminhoArquivo;

    public Biblioteca() {
        this(ARQUIVO_PADRAO);
    }

    public Biblioteca(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public List<Material> getMateriais() {
        return Collections.unmodifiableList(materiais);
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public List<Emprestimo> getEmprestimos() {
        return Collections.unmodifiableList(emprestimos);
    }

    // ------------------------------------------------------------------ Materiais

    public void cadastrarMaterial(Material material) throws BibliotecaException {
        if (material == null) {
            throw new BibliotecaException("Material não informado.");
        }
        material.validar();
        if (buscarMaterialOuNull(material.getCodigo()) != null) {
            throw new BibliotecaException("Já existe um material com o código " + material.getCodigo() + ".");
        }
        materiais.add(material);
    }

    public Material buscarMaterialOuNull(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Material m : materiais) {
            if (m.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return m;
            }
        }
        return null;
    }

    public Material buscarMaterial(String codigo) throws BibliotecaException {
        Material m = buscarMaterialOuNull(codigo);
        if (m == null) {
            throw new BibliotecaException("Material não encontrado: " + (codigo == null ? "" : codigo.trim()) + ".");
        }
        return m;
    }

    /** altera material de mesmo código */
    public void alterarMaterial(Material novo) throws BibliotecaException {
        if (novo == null) {
            throw new BibliotecaException("Material não informado.");
        }
        novo.validar();
        buscarMaterial(novo.getCodigo()).atualizarDe(novo);
    }

    public void removerMaterial(String codigo) throws BibliotecaException {
        Material m = buscarMaterial(codigo);
        for (Emprestimo e : emprestimos) {
            if (e.getMaterial() == m && e.estaEmAberto()) {
                throw new BibliotecaException("Não é possível remover: o material possui empréstimo em aberto.");
            }
        }
        materiais.remove(m);
    }

    public ArrayList<Material> listarMateriais() {
        ArrayList<Material> lista = new ArrayList<>(materiais);
        lista.sort(Comparator.comparing(m -> Util.normalizar(m.getTitulo())));
        return lista;
    }

    // ------------------------------------------------------------------ Usuários

    public void cadastrarUsuario(Usuario usuario) throws BibliotecaException {
        if (usuario == null) {
            throw new BibliotecaException("Usuário não informado.");
        }
        usuario.validar();
        if (buscarUsuarioOuNull(usuario.getCpfMatricula()) != null) {
            throw new BibliotecaException("Já existe um usuário com o CPF/matrícula " + usuario.getCpfMatricula() + ".");
        }
        usuarios.add(usuario);
    }

    public Usuario buscarUsuarioOuNull(String cpfMatricula) {
        if (cpfMatricula == null) {
            return null;
        }
        String chave = Util.chave(cpfMatricula);
        for (Usuario u : usuarios) {
            if (Util.chave(u.getCpfMatricula()).equals(chave)) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscarUsuario(String cpfMatricula) throws BibliotecaException {
        Usuario u = buscarUsuarioOuNull(cpfMatricula);
        if (u == null) {
            throw new BibliotecaException("Usuário não encontrado: " + (cpfMatricula == null ? "" : cpfMatricula.trim()) + ".");
        }
        return u;
    }

    public void alterarUsuario(String cpfMatricula, String nome, String telefone, String email)
            throws BibliotecaException {
        buscarUsuario(cpfMatricula).atualizarContato(nome, telefone, email);
    }

    /** remoção = inativação */
    public void inativarUsuario(String cpfMatricula) throws BibliotecaException {
        Usuario u = buscarUsuario(cpfMatricula);
        if (!u.isAtivo()) {
            throw new BibliotecaException("O usuário já está inativo.");
        }
        if (u.contarEmprestimosEmAberto() > 0) {
            throw new BibliotecaException("Não é possível inativar: o usuário possui empréstimo em aberto.");
        }
        if (u.temMultaPendente()) {
            throw new BibliotecaException("Não é possível inativar: o usuário possui multa pendente.");
        }
        u.setAtivo(false);
    }

    public void reativarUsuario(String cpfMatricula) throws BibliotecaException {
        Usuario u = buscarUsuario(cpfMatricula);
        if (u.isAtivo()) {
            throw new BibliotecaException("O usuário já está ativo.");
        }
        u.setAtivo(true);
    }

    public ArrayList<Usuario> listarUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>(usuarios);
        lista.sort(Comparator.comparing(u -> Util.normalizar(u.getNome())));
        return lista;
    }

    // ------------------------------------------------------------------ Empréstimos

    public Emprestimo registrarEmprestimo(Usuario usuario, Material material) throws BibliotecaException {
        return registrarEmprestimo(usuario, material, new Date());
    }

    public Emprestimo registrarEmprestimo(Usuario usuario, Material material, Date data)
            throws BibliotecaException {
        if (usuario == null || !usuarios.contains(usuario)) {
            throw new BibliotecaException("Usuário não cadastrado.");
        }
        if (material == null || !materiais.contains(material)) {
            throw new BibliotecaException("Material não cadastrado.");
        }
        if (!material.podeSerEmprestado()) {
            throw new BibliotecaException("O material \"" + material.getTitulo()
                    + "\" é somente para consulta local e não pode ser emprestado.");
        }
        if (!material.estaDisponivel()) {
            throw new BibliotecaException("Não há exemplar disponível de \"" + material.getTitulo() + "\".");
        }
        String motivo = usuario.motivoBloqueio(data);
        if (motivo != null) {
            throw new BibliotecaException("Empréstimo bloqueado: " + motivo);
        }
        Emprestimo e = new Emprestimo(usuario, material, data);
        material.registrarEmprestimo();
        usuario.adicionarEmprestimo(e);
        emprestimos.add(e);
        return e;
    }

    public Emprestimo buscarEmprestimo(int codigo) throws BibliotecaException {
        for (Emprestimo e : emprestimos) {
            if (e.getCodigo() == codigo) {
                return e;
            }
        }
        throw new BibliotecaException("Empréstimo não encontrado: " + codigo + ".");
    }

    public ArrayList<Emprestimo> listarEmprestimosEmAberto() {
        ArrayList<Emprestimo> lista = new ArrayList<>();
        for (Emprestimo e : emprestimos) {
            if (e.estaEmAberto()) {
                lista.add(e);
            }
        }
        return lista;
    }

    public ArrayList<Penalidade> registrarDevolucao(Emprestimo emprestimo, EstadoMaterial estado)
            throws BibliotecaException {
        return registrarDevolucao(emprestimo, estado, new Date());
    }

    /** encerra, penaliza, atualiza disponibilidade */
    public ArrayList<Penalidade> registrarDevolucao(Emprestimo emprestimo, EstadoMaterial estado, Date data)
            throws BibliotecaException {
        if (emprestimo == null || !emprestimos.contains(emprestimo)) {
            throw new BibliotecaException("Empréstimo não cadastrado.");
        }
        ArrayList<Penalidade> geradas = emprestimo.encerrar(estado, data);
        if (estado == EstadoMaterial.EXTRAVIADO) {
            emprestimo.getMaterial().registrarExtravio();
        } else {
            emprestimo.getMaterial().registrarDevolucao();
        }
        return geradas;
    }

    // ------------------------------------------------------------------ Multas

    public void registrarQuitacao(Multa multa) throws BibliotecaException {
        registrarQuitacao(multa, new Date());
    }

    public void registrarQuitacao(Multa multa, Date data) throws BibliotecaException {
        if (multa == null) {
            throw new BibliotecaException("Multa não informada.");
        }
        if (multa.isQuitada()) {
            throw new BibliotecaException("Esta multa já foi quitada.");
        }
        multa.quitar(data);
    }

    public void registrarQuitacao(Usuario usuario, int codigoMulta) throws BibliotecaException {
        for (Multa m : usuario.getMultasPendentes()) {
            if (m.getCodigo() == codigoMulta) {
                registrarQuitacao(m);
                return;
            }
        }
        throw new BibliotecaException("Multa pendente não encontrada para este usuário: " + codigoMulta + ".");
    }

    // ------------------------------------------------------------------ Consultas

    /** busca por texto, tipo, disponibilidade */
    public ArrayList<Material> consultarAcervo(String texto, String tipo, boolean apenasDisponiveis) {
        String busca = Util.normalizar(texto);
        String tipoBusca = Util.normalizar(tipo);
        boolean todosTipos = tipoBusca.isEmpty() || tipoBusca.equals("todos");
        ArrayList<Material> resultado = new ArrayList<>();
        for (Material m : listarMateriais()) {
            if (!todosTipos && !Util.normalizar(m.getTipo()).equals(tipoBusca)) {
                continue;
            }
            if (apenasDisponiveis && !m.estaDisponivel()) {
                continue;
            }
            if (!busca.isEmpty()) {
                boolean achou = Util.normalizar(m.getTitulo()).contains(busca)
                        || Util.normalizar(m.getCodigo()).contains(busca)
                        || Util.normalizar(m.getAssunto()).contains(busca)
                        || (m instanceof Livro && Util.normalizar(((Livro) m).getAutor()).contains(busca));
                if (!achou) {
                    continue;
                }
            }
            resultado.add(m);
        }
        return resultado;
    }

    /** menor data prevista em aberto, ou null */
    public Date getPrevisaoDevolucao(Material material) {
        Date menor = null;
        for (Emprestimo e : emprestimos) {
            if (e.getMaterial() == material && e.estaEmAberto()
                    && (menor == null || e.getDataPrevista().before(menor))) {
                menor = e.getDataPrevista();
            }
        }
        return menor;
    }

    /** histórico filtrado; filtros opcionais */
    public ArrayList<Emprestimo> consultarHistorico(Usuario usuario, Material material, Date inicio, Date fim,
                                                    String situacao, Date referencia) {
        ArrayList<Emprestimo> resultado = new ArrayList<>();
        for (Emprestimo e : emprestimos) {
            if (usuario != null && e.getUsuario() != usuario) {
                continue;
            }
            if (material != null && e.getMaterial() != material) {
                continue;
            }
            if (inicio != null && Util.diasEntre(inicio, e.getDataEmprestimo()) < 0) {
                continue;
            }
            if (fim != null && Util.diasEntre(e.getDataEmprestimo(), fim) < 0) {
                continue;
            }
            if (!Util.vazio(situacao) && !situacao.equalsIgnoreCase("Todas")) {
                boolean ok = situacao.equalsIgnoreCase(Emprestimo.EM_ATRASO)
                        ? e.estaEmAtraso(referencia)
                        : e.getSituacao().equalsIgnoreCase(situacao);
                if (!ok) {
                    continue;
                }
            }
            resultado.add(e);
        }
        return resultado;
    }

    // ------------------------------------------------------------------ Arquivos

    /** grava tudo em um arquivo */
    public void salvarArquivos() throws BibliotecaException {
        File destino = new File(caminhoArquivo).getAbsoluteFile();
        File pasta = destino.getParentFile();
        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            throw new BibliotecaException("Não foi possível criar a pasta de dados: " + pasta);
        }
        File temporario = new File(destino.getPath() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(temporario)))) {
            out.writeObject(materiais);
            out.writeObject(usuarios);
            out.writeObject(emprestimos);
        } catch (IOException e) {
            throw new BibliotecaException("Erro ao gravar o arquivo de dados: " + e.getMessage());
        }
        try {
            Files.move(temporario.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BibliotecaException("Erro ao gravar o arquivo de dados: " + e.getMessage());
        }
    }

    /** lê arquivo, substitui listas */
    @SuppressWarnings("unchecked")
    public void carregarArquivos() throws BibliotecaException {
        File origem = new File(caminhoArquivo);
        if (!origem.exists()) {
            return;
        }
        ArrayList<Material> lidosMateriais;
        ArrayList<Usuario> lidosUsuarios;
        ArrayList<Emprestimo> lidosEmprestimos;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(origem)))) {
            lidosMateriais = (ArrayList<Material>) in.readObject();
            lidosUsuarios = (ArrayList<Usuario>) in.readObject();
            lidosEmprestimos = (ArrayList<Emprestimo>) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new BibliotecaException("Erro ao ler o arquivo de dados (" + caminhoArquivo + "): " + e.getMessage());
        }
        materiais.clear();
        materiais.addAll(lidosMateriais);
        usuarios.clear();
        usuarios.addAll(lidosUsuarios);
        emprestimos.clear();
        emprestimos.addAll(lidosEmprestimos);

        // retoma numeração
        int maiorEmprestimo = 0;
        for (Emprestimo e : emprestimos) {
            maiorEmprestimo = Math.max(maiorEmprestimo, e.getCodigo());
        }
        int maiorPenalidade = 0;
        for (Usuario u : usuarios) {
            for (Penalidade p : u.getPenalidades()) {
                maiorPenalidade = Math.max(maiorPenalidade, p.getCodigo());
            }
        }
        Emprestimo.garantirProximoCodigo(maiorEmprestimo + 1);
        Penalidade.garantirProximoCodigo(maiorPenalidade + 1);
    }
}
