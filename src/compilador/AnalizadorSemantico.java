package compilador;

public class AnalizadorSemantico {

    private IdentificadorBean[] tabla;
    private MensajeError mensajeError;

    public AnalizadorSemantico(MensajeError mensajeError) {
        tabla = new IdentificadorBean[Constantes.MAXIDENT];
        this.mensajeError = mensajeError;
    }

    public IdentificadorBean buscar(String identificador, int desde, int hasta) {
        IdentificadorBean id = null;

        int i = desde;
        
        while (i >= hasta && i >= 0 && i < tabla.length) {
            if (tabla[i].getNombre().toLowerCase().equals(identificador.toLowerCase())) {
                id = tabla[i];
                break;
            }
            i--;
        }
        return id;
    }

    public void cargar(int posicion, String nombre, Terminal tipo, int valor) {
        if (posicion >= 256) {
            mensajeError.mostrar(17);
        } else {
            tabla[posicion] = new IdentificadorBean(nombre, tipo, valor);
        }
    }
}
