package biblioteca;

/** estado na devolução */
public enum EstadoMaterial {
    BOM("Em bom estado"),
    DANIFICADO("Danificado"),
    EXTRAVIADO("Extraviado");

    private final String rotulo;

    EstadoMaterial(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static EstadoMaterial deRotulo(String rotulo) {
        for (EstadoMaterial e : values()) {
            if (e.rotulo.equalsIgnoreCase(rotulo)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado inválido: " + rotulo);
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
