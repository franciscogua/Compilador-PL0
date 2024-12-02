package compilador;

import java.io.IOException;
import javax.swing.JFileChooser;

public class Main {

    private static AnalizadorLexico alex;
    private static AnalizadorSintactico asin;
    private static AnalizadorSemantico asem;
    private static MensajeError mensajeError;
    private static GeneradorDeCodigo gencod;

    public static void main(String[] args) throws IOException {
        try {
            String fileName = "";

            JFileChooser fc = new JFileChooser("");
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getPath();
            }

            if (fileName.isEmpty()) {
                System.out.println("NO SE HA CARGADO NINGUN ARCHIVO");
            } else {
                alex = new AnalizadorLexico(fileName);
                asem = new AnalizadorSemantico(mensajeError);
                gencod = new GeneradorDeCodigo(fileName + ".exe");
                mensajeError = new MensajeError();
                asin = new AnalizadorSintactico(alex, asem, gencod, mensajeError);
                asin.analizar();
                alex.close();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
