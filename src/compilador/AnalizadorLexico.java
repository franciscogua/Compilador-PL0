package compilador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static compilador.Terminal.*;

public class AnalizadorLexico {

    private MensajeError mensajeError;
    private BufferedReader reader;
    private int numLinea = 1;
    private StringBuilder listado; 
    private Queue<String> tokens; 
    private String tokenActual;
    private String tokenProcesado;

    public AnalizadorLexico(String fileName) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("El archivo " + fileName + " no existe.");
        }

        mensajeError = new MensajeError();
        reader = new BufferedReader(new FileReader(file));
        listado = new StringBuilder();
        tokens = new LinkedList<>();
    }

    private boolean esCadena(String token) {
        return token.startsWith("'") && token.endsWith("'");
    }

    public Terminal escanear() throws IOException {
        if (tokenActual == null) {
            while (tokens.isEmpty()) { // Si la cola está vacía, leer una nueva línea
                String line = reader.readLine();
                if (line == null) {
                    return Terminal.EOF; // Fin del archivo
                }
                listado.append(numLinea).append(": ").append(line).append("\n");
                numLinea++;

                Pattern pattern = Pattern.compile("'[^']*'|\\b\\w+\\b|:=|<=|>=|<>|[\\p{Punct}]");
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    tokens.add(matcher.group());
                }
            }

            tokenActual = tokens.poll(); // Obtener el siguiente token de la cola
            System.out.println("Token encontrado: " + tokenActual);
        }
        
        tokenProcesado = tokenActual;
        tokenActual = null;

        
        // Verifica si el token es una cadena
        if (esCadena(tokenProcesado)) {
            return Terminal.CADENA_LITERAL;
        }

        // Verifica si es palabra reservada
        Terminal posiblePalabraReservada = esPalabraReservada(tokenProcesado);
        if (posiblePalabraReservada != NULO) {
            return posiblePalabraReservada;
        }

        // Verifica si es un número
        if (esNumero(tokenProcesado)) {
            return NUMERO;
            // numAux = Integer.parseInt(token); 
        }

        // Verifica si es asignación (:=)
        if (esAsignacion(tokenProcesado)) {
            return ASIGNACION;
        }

        // Verifica si es un operador de comparación (>=, <=, etc.)
        Terminal comparacion = obtenerComparacion(tokenProcesado);
        if (comparacion != NULO) {
            return comparacion;
        }

        // Verifica si es inicialización (=)
        if (esInicializacion(tokenProcesado)) {
            return IGUAL;
        }

        // Verifica si es un identificador
        if (esIdentificador(tokenProcesado)) {
            return IDENTIFICADOR;
        }

        // Verifica si es un separador (.,;, etc.)
        Terminal separador = obtenerSeparador(tokenProcesado);
        if (separador != NULO) {
            return separador;
        }

        // Verifica si es un operador aritmético (+, -, *, /)
        Terminal operador = obtenerOperador(tokenProcesado);
        if (operador != NULO) {
            return operador;
        }

        // Verifica si es apertura o cierre de paréntesis
        if (abreParentesis(tokenProcesado)) {
            return ABRE_PARENTESIS;
        }
        if (cierraParentesis(tokenProcesado)) {
            return CIERRA_PARENTESIS;
        }

        // Si ningún caso coincide, devuelve error
        mensajeError.mostrar(1);
        return NULO; // Devuelve NUL si hay un error
    }

    public String getCad() throws IOException {
        return tokenProcesado; // Obtiene la cadena correspondiente al token actual
    }

    private Terminal esPalabraReservada(String token) {
        return switch (token.toLowerCase()) {
            case "begin" ->
                BEGIN;
            case "call" ->
                CALL;
            case "const" ->
                CONST;
            case "do" ->
                DO;
            case "end" ->
                END;
            case "if" ->
                IF;
            case "odd" ->
                ODD;
            case "procedure" ->
                PROCEDURE;
            case "then" ->
                THEN;
            case "var" ->
                VAR;
            case "while" ->
                WHILE;
            case "write" ->
                WRITE;
            case "readln" ->
                READLN;
            case "writeln" ->
                WRITELN;
            case "not" ->
                NOT;
            case "halt" ->
                HALT;
            case "for" ->
                FOR;
            case "to" ->
                TO;
            case "repeat" ->
                REPEAT;
            case "times" ->
                TIMES;
            case "sqr" ->
                SQR;
            default ->
                NULO;
        };
    }

    private boolean esNumero(String token) {
        return token.matches("\\d+");
    }

    private boolean esAsignacion(String token) {
        return ":=".equals(token);
    }

    private Terminal obtenerComparacion(String token) {
        return switch (token) {
            case "<=" ->
                MENOR_IGUAL;
            case ">=" ->
                MAYOR_IGUAL;
            case "<>" ->
                DISTINTO;
            case "<" ->
                MENOR;
            case ">" ->
                MAYOR;
            default ->
                NULO;
        };
    }

    private Terminal obtenerOperador(String token) {
        System.out.println("OPERADOR OBTENIDO: " + token);
        return switch (token) {
            case "+" ->
                MAS;
            case "-" ->
                MENOS;
            case "*" ->
                POR;
            case "/" ->
                DIVIDIDO;
            default ->
                NULO;
        };
    }

    private Terminal obtenerSeparador(String token) {
        return switch (token) {
            case "," ->
                COMA;
            case ";" ->
                PUNTO_Y_COMA;
            case "." ->
                PUNTO;
            default ->
                NULO;
        };
    }

    private boolean esInicializacion(String token) {
        return "=".equals(token);
    }

    private boolean esIdentificador(String token) {
        return token.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private boolean abreParentesis(String token) {
        return "(".equals(token);
    }

    private boolean cierraParentesis(String token) {
        return ")".equals(token);
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
