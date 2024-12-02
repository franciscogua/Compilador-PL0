package compilador;

import static compilador.Terminal.*;
import static compilador.Constantes.*;
import java.io.IOException;

public class AnalizadorSintactico {

    private AnalizadorLexico alex;
    private AnalizadorSemantico asem;
    private GeneradorDeCodigo gencod;
    private MensajeError mensajeError;
    private Terminal simb;
    private int contador;

    public AnalizadorSintactico(AnalizadorLexico alex, AnalizadorSemantico asem, GeneradorDeCodigo gencod, MensajeError mensajeError) throws IOException {
        this.alex = alex;
        this.asem = asem;
        this.gencod = gencod;
        this.mensajeError = mensajeError;
        contador = 0;
    }

    public void analizar() throws IOException {
        simb = alex.escanear();
        programa();

        escaneoSimple(simb, EOF, 28);
    }

    public void programa() throws IOException {
        gencod.cargarParteTamFijo();

        bloque(0);
        escaneoSimple(simb, PUNTO, 2);

        gencod.cargarFinalDelPrograma();
        gencod.reemplazarInicio();
        gencod.iniciarVars(contador);
        gencod.reemplazarTamanoVirtual();
        gencod.llenarCeros();
        gencod.reemplazarTamanoSeccCod();
        gencod.reemplazarDataPura();
        gencod.reemplazarTamanoImgyBaseDato();
        gencod.volcarMemoria();
    }

    private void bloque(int base) throws IOException {
        System.out.println("Bloque - Token actual: " + simb);

        int desplazamiento = 0;
        gencod.cargarSalto(0);
        int posSalto = gencod.traerTop();

        if (simb == CONST) {
            do {
                simb = alex.escanear();
                asignarConstante(base, desplazamiento);
                desplazamiento++;
            } while (simb == COMA);
            escaneoSimple(simb, PUNTO_Y_COMA, 7);
        }

        if (simb == VAR) {
            simb = alex.escanear();
            String nombre = alex.getCad();
            escaneoSimple(simb, IDENTIFICADOR, 4);

            IdentificadorBean identificador = asem.buscar(nombre, base + desplazamiento - 1, base);
            if (identificador != null) {
                mensajeError.mostrar(19); // Error: identificador repetido
            } else {
                asem.cargar(base + desplazamiento, nombre, VAR, contador * Constantes.TAMANIO_BEAN);
                contador++;
                desplazamiento++;

                while (simb == Terminal.COMA) {
                    simb = alex.escanear();
                    nombre = alex.getCad();
                    escaneoSimple(simb, IDENTIFICADOR, 4);

                    identificador = asem.buscar(nombre, base + desplazamiento - 1, base);

                    if (identificador != null) {
                        mensajeError.mostrar(19); // Error: identificador repetido
                    } else {
                        asem.cargar(base + desplazamiento, nombre, VAR, contador * Constantes.TAMANIO_BEAN);
                        contador++;
                        desplazamiento++;
                    }
                }
                escaneoSimple(simb, PUNTO_Y_COMA, 7);
            }
        }

        while (simb == Terminal.PROCEDURE) {
            simb = alex.escanear();
            String nombre = alex.getCad();
            escaneoSimple(simb, IDENTIFICADOR, 4);
            IdentificadorBean identificador = asem.buscar(nombre, base + desplazamiento - 1, base);

            if (identificador != null) {
                mensajeError.mostrar(19); // Error: identificador repetido
            } else {
                asem.cargar(base + desplazamiento, nombre, PROCEDURE, gencod.traerTop());
                desplazamiento++;

                escaneoSimple(simb, PUNTO_Y_COMA, 7);
                bloque(base + desplazamiento);
                gencod.cargarBloqueFinalProcedure();
                escaneoSimple(simb, PUNTO_Y_COMA, 7);
            }
        }

        if (gencod.traerTop() - posSalto != 0) {
            gencod.reemplazarInt(posSalto - 4, gencod.traerTop() - posSalto);
        }

        proposicion(base, desplazamiento);
    }

    private void proposicion(int base, int desplazamiento) throws IOException {
        System.out.println("Proposicion - Token actual: " + simb);

        switch (simb) {
            case IDENTIFICADOR -> {
                IdentificadorBean identificador = asem.buscar(alex.getCad(), base + desplazamiento - 1, 0);

                if (identificador == null) {
                    mensajeError.mostrar(20); // Error: identificador no declarado
                } else if (identificador.getTipo() == PROCEDURE || identificador.getTipo() == CONST) {
                    mensajeError.mostrar(16); // Error: se esperaba una variable
                } else {
                    simb = alex.escanear();
                }
                escaneoSimple(simb, ASIGNACION, 8);
                expresion(base, desplazamiento);
                gencod.cargarAsignacion(identificador.getValor());
            }

            case CALL -> {
                simb = alex.escanear();
                IdentificadorBean identificador = asem.buscar(alex.getCad(), base + desplazamiento - 1, 0);

                if (identificador == null) {
                    mensajeError.mostrar(21); // Error: procedure no declarado
                } else if (identificador.getTipo() == VAR || identificador.getTipo() == CONST) {
                    mensajeError.mostrar(22); // Error: se esperaba un procedure
                } else {
                    escaneoSimple(simb, IDENTIFICADOR, 4);
                    gencod.cargarCall(identificador.getValor());
                }
            }

            case BEGIN -> {
                simb = alex.escanear();
                proposicion(base, desplazamiento);

                while (simb == PUNTO_Y_COMA) {
                    simb = alex.escanear();
                    if (simb == HALT) {
                        simb = alex.escanear();
                        break;
                    }
                    proposicion(base, desplazamiento);
                }
                escaneoSimple(simb, END, 10);
            }

            case IF -> {
                int posicionSaltoSiEsVerdadero = 0;
                simb = alex.escanear();

                if (simb == NOT) {
                    simb = alex.escanear();
                    condicion(base, desplazamiento);

                    gencod.reemplazarInt(gencod.traerTop() - 4, 5);
                    gencod.cargarSalto(0);

                    posicionSaltoSiEsVerdadero = gencod.traerTop();
                    escaneoSimple(simb, THEN, 11);
                } else {
                    condicion(base, desplazamiento);
                    int punto = gencod.traerTop();
                    escaneoSimple(simb, THEN, 11);
                    proposicion(base, desplazamiento);

                    int destino = gencod.traerTop();
                    int distancia = destino - punto;
                    gencod.reemplazarInt((punto - 4), distancia);
                }
                /*
                proposicion(base, desplazamiento);

                gencod.cargarHalt();

                int finDeProposicion = gencod.traerTop();
                int distanciaSalto = finDeProposicion - posicionSaltoSiEsVerdadero;

                gencod.reemplazarInt(posicionSaltoSiEsVerdadero - 4, distanciaSalto);
                 */
            }

            case WHILE -> {
                simb = alex.escanear();
                int preSalto = gencod.traerTop();
                condicion(base, desplazamiento);
                int punto = gencod.traerTop();
                escaneoSimple(simb, DO, 12);
                proposicion(base, desplazamiento);
                int saltoDistancia = preSalto - (gencod.traerTop() + 5);
                gencod.cargarSalto(saltoDistancia);
                int destino = gencod.traerTop();
                int distancia = destino - punto;
                gencod.reemplazarInt((punto - 4), distancia);
            }

            case READLN -> {
                simb = alex.escanear();
                escaneoSimple(simb, ABRE_PARENTESIS, 23);
                IdentificadorBean identificador = asem.buscar(alex.getCad(), base + desplazamiento - 1, 0);

                if (identificador == null) {
                    mensajeError.mostrar(24); // Error: variable no declarada
                } else if (identificador.getTipo() == PROCEDURE || identificador.getTipo() == CONST) {
                    mensajeError.mostrar(16); // Error: se esperaba una variable
                } else {
                    escaneoSimple(simb, IDENTIFICADOR, 4);
                    gencod.cargarReadln(identificador.getValor());
                }

                while (simb == COMA) {
                    simb = alex.escanear();
                    identificador = asem.buscar(alex.getCad(), base + desplazamiento - 1, 0);

                    if (identificador == null) {
                        mensajeError.mostrar(24); // Error: variable no declarada
                    } else if (identificador.getTipo() == VAR || identificador.getTipo() == CONST) {
                        mensajeError.mostrar(16); // Error: se esperaba una variable
                    } else {
                        escaneoSimple(simb, IDENTIFICADOR, 4);
                        gencod.cargarReadln(identificador.getValor());
                    }

                }
                escaneoSimple(simb, CIERRA_PARENTESIS, 13);
            }
            case WRITE -> {
                simb = alex.escanear();
                escaneoSimple(simb, ABRE_PARENTESIS, 23);

                if (simb == Terminal.CADENA_LITERAL) {
                    gencod.cargarWriteString(alex.getCad());
                    simb = alex.escanear();
                } else {
                    expresion(base, desplazamiento);
                    gencod.cargarSalidaExpresion();
                }

                while (simb == COMA) {
                    simb = alex.escanear();

                    if (simb == Terminal.CADENA_LITERAL) {
                        gencod.cargarWriteString(alex.getCad());
                        simb = alex.escanear();
                    } else {
                        expresion(base, desplazamiento);
                        gencod.cargarSalidaExpresion();
                    }
                }
                escaneoSimple(simb, CIERRA_PARENTESIS, 13);
            }
            case WRITELN -> {
                simb = alex.escanear();
                if (simb == ABRE_PARENTESIS) {
                    simb = alex.escanear();
                    if (simb == CADENA_LITERAL) {
                        gencod.cargarWriteString(alex.getCad());
                        simb = alex.escanear();
                    } else {
                        expresion(base, desplazamiento);
                        gencod.cargarSalidaExpresion();
                    }

                    while (simb == COMA) {
                        simb = alex.escanear();
                        if (simb == CADENA_LITERAL) {
                            gencod.cargarWriteString(alex.getCad());
                            simb = alex.escanear();
                        } else {
                            expresion(base, desplazamiento);
                            gencod.cargarSalidaExpresion();
                        }
                    }
                    escaneoSimple(simb, CIERRA_PARENTESIS, 13);
                }
                gencod.cargarCall(SALTO_DE_LINEA);
            }
            case FOR -> {
                simb = alex.escanear();

                if (simb != IDENTIFICADOR) {
                    mensajeError.mostrar(4); // Se esperaba un identificador
                }

                String nombre = alex.getCad();

                proposicion(base, desplazamiento);

                escaneoSimple(simb, TO, 29);

                int condicionCierre = Integer.parseInt(alex.getCad());

                expresion(base, desplazamiento);

                escaneoSimple(simb, DO, 12);

                int beginFor = gencod.traerTop();

                IdentificadorBean identificador = asem.buscar(nombre, base + desplazamiento - 1, 0);
                gencod.cargarFor(identificador.getValor() * 4, condicionCierre);

                int inicioProposicionFor = gencod.traerTop();
                proposicion(base, desplazamiento);
                int finProposicionFor = gencod.traerTop();
                int distanciaSaltoFor = (finProposicionFor + 30) - inicioProposicionFor;

                gencod.reemplazarInt(inicioProposicionFor - 4, distanciaSaltoFor);

                gencod.incrementar(identificador.getValor() * 4);

                gencod.cargarSalto(beginFor - (gencod.traerTop() + 5));
            }
            case REPEAT -> {
                simb = alex.escanear();
                expresion(base, desplazamiento);

                if (simb != TIMES) {
                    mensajeError.mostrar(30);
                }

                int beginRepeat = gencod.traerTop();
                gencod.cargarRepeat();
                int inicioProposicionRepeat = gencod.traerTop();

                simb = alex.escanear();
                proposicion(base, desplazamiento);

                int finProposicionRepeat = gencod.traerTop();
                int distanciaSaltoRepeat = (finProposicionRepeat + 5) - inicioProposicionRepeat;

                gencod.reemplazarInt(inicioProposicionRepeat - 4, distanciaSaltoRepeat);
                gencod.cargarSalto(beginRepeat - (gencod.traerTop() + 5));
            }
        }
    }

    private void condicion(int base, int desplazamiento) throws IOException {
        System.out.println("Condicion - Token actual: " + simb);

        if (simb == Terminal.ODD) {
            simb = alex.escanear();
            expresion(base, desplazamiento);
            gencod.impar();
        } else {
            expresion(base, desplazamiento);
            Terminal operador = simb;
            switch (simb) {
                case IGUAL -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                case DISTINTO -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                case MENOR -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                case MENOR_IGUAL -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                case MAYOR -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                case MAYOR_IGUAL -> {
                    simb = alex.escanear();
                    expresion(base, desplazamiento);
                    gencod.cargarCondicion(operador);
                }

                default ->
                    mensajeError.mostrar(15);
            }
        }
    }

    private void expresion(int base, int desplazamiento) throws IOException {
        System.out.println("Expresion - Token actual: " + simb);

        Terminal operador = null;

        if (simb == MAS || simb == MENOS) {
            operador = simb;
            simb = alex.escanear();
        }

        termino(base, desplazamiento);

        if (operador == MENOS) {
            gencod.menosUnario();
        }

        while (simb == MAS || simb == MENOS) {
            operador = simb;
            simb = alex.escanear();
            termino(base, desplazamiento);

            if (operador == MAS) {
                gencod.suma();
            } else {
                gencod.resta();
            }
        }
    }

    private void termino(int base, int desplazamiento) throws IOException {
        System.out.println("Termino - Token actual: " + simb);
        Terminal operador = null;

        factor(base, desplazamiento);
        while (simb == POR || simb == DIVIDIDO) {
            operador = simb;
            simb = alex.escanear();
            factor(base, desplazamiento);

            if (operador == POR) {
                gencod.multiplicar();
            } else {
                gencod.dividir();
            }
        }
    }

    private void factor(int base, int desplazamiento) throws IOException {
        System.out.println("Expresion - Token actual: " + simb);

        switch (simb) {
            case IDENTIFICADOR:
                String nombre = alex.getCad();
                IdentificadorBean identificador = asem.buscar(nombre, base + desplazamiento - 1, 0);

                if (identificador == null) {
                    mensajeError.mostrar(20); // Error: identificador no declarado
                } else if (identificador.getTipo() == PROCEDURE) {
                    mensajeError.mostrar(25); // Error: se esperaba una constante o variable
                } else {
                    simb = alex.escanear();
                    if (identificador.getTipo() == VAR) {
                        gencod.generarFactorVar(identificador.getValor());
                    } else {
                        gencod.generarFactorNum(identificador.getValor());
                    }
                }
                break;
            case NUMERO:
                String valor = alex.getCad();
                simb = alex.escanear();
                gencod.generarFactorNum(Integer.parseInt(valor));
                break;
            case ABRE_PARENTESIS:
                simb = alex.escanear();
                expresion(base, desplazamiento);
                escaneoSimple(simb, CIERRA_PARENTESIS, 9);
                break;

            case SQR:
                simb = alex.escanear();
                escaneoSimple(simb, ABRE_PARENTESIS, 23);
                nombre = alex.getCad();
                identificador = asem.buscar(nombre, base + desplazamiento - 1, 0);
                if (identificador == null) {
                    mensajeError.mostrar(20);
                } else if (identificador.getTipo() == PROCEDURE || identificador.getTipo() == CONST) {
                    mensajeError.mostrar(16);
                } else {
                    expresion(base, desplazamiento);
                }
                escaneoSimple(simb, CIERRA_PARENTESIS, 9);
                gencod.cargarSqr();
                break;

            default:
                mensajeError.mostrar(17); // Error: factor no válido
                break;
        }
    }

    private void escaneoSimple(Terminal recibida, Terminal esperada, int cod) throws IOException {

        if (recibida == esperada) {
            simb = alex.escanear();
        } else {
            mensajeError.mostrar(cod);
        }

        if (recibida == EOF) {
            mensajeError.mostrar(0); // Fin del archivo esperado
        }
    }

    private void asignarConstante(int base, int desp) throws IOException {

        String nombre = alex.getCad();
        escaneoSimple(simb, IDENTIFICADOR, 4);
        IdentificadorBean identificador = asem.buscar(nombre, base + desp - 1, base);

        if (identificador != null) {
            mensajeError.mostrar(18);
        } else {

            escaneoSimple(simb, IGUAL, 5);
            String valor = alex.getCad();
            if (valor.equals("-")) {
                simb = alex.escanear();
                valor += alex.getCad();
            }
            escaneoSimple(simb, NUMERO, 6);
            asem.cargar(base + desp, nombre, CONST, Integer.parseInt(valor));
        }
    }
}
