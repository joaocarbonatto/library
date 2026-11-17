# Sistema de Biblioteca (POO, Java)

Controle de acervo (livros, revistas e outras publicações), usuários, empréstimos, devoluções, multas e suspensões.
Interface gráfica em `JOptionPane`. Dados gravados em `dados/biblioteca.dat`.

## Executar

Requer Java 8 ou superior. Os arquivos são UTF-8 (na IDE, defina a codificação do projeto como UTF-8).

    ./compilar_e_executar.sh        (Linux/macOS)
    compilar_e_executar.bat         (Windows)

Ou, numa IDE: pasta `src` como source root, executar `biblioteca.Principal`.
Na primeira execução o sistema oferece carregar dados de exemplo.

Para simular atrasos na apresentação, os formulários de empréstimo e devolução aceitam uma data opcional (dd/MM/aaaa, não futura).

## Testes unitários

JUnit 5 (Jupiter). Pasta `test`, classes `BibliotecaTest` e `ModeloTest` (41 testes). Na IDE: adicionar a biblioteca JUnit 5 e executar a pasta `test`.

## Onde está cada item

| Item | Onde |
|---|---|
| e) classes conforme o diagrama | `src/biblioteca/*.java` |
| f) código automático e sequencial (atributo estático no construtor) | `Emprestimo.proximoCodigo`, `Penalidade.proximoCodigo` |
| g) arquivos (leitura/gravação de objetos) | `Biblioteca.salvarArquivos()` / `carregarArquivos()` (serialização) |
| h) classe abstrata | `Material`, `Usuario`, `Penalidade` |
| i) inclusão, exclusão, alteração, consulta, lista | `Biblioteca` + menus de `InterfaceBiblioteca` |
| j) interface gráfica | `InterfaceBiblioteca` (`JOptionPane`) |
| k) testes unitários | `test/biblioteca` |
| l) exceções, com classe própria | `BibliotecaException` |
| m) listas | `ArrayList` de materiais, usuários, empréstimos e penalidades |

## Regras de negócio

Prazo: livro 14 dias, revista 7, outra publicação 5; professor +7. Atraso: livro R$ 1,00/dia, revista R$ 2,00/dia, outra publicação suspensão de 2 dias por dia de atraso. Limite de empréstimos em aberto: aluno 3, professor 5, externo 2. Dano: multa de 50% do valor de reposição. Extravio: multa de 100% e o exemplar sai do acervo (não soma multa de atraso). Dano e atraso na mesma devolução somam as duas penalidades.
