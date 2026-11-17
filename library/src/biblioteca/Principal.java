package biblioteca;

import javax.swing.UIManager;

/** ponto de entrada */
public class Principal {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // visual padrão
        }
        new InterfaceBiblioteca(new Biblioteca()).iniciar();
    }
}
