package compilador;

public class MensajeError {

    public void mostrar(int codigo) {
        if (codigo == 0) {
            System.out.println("Muy bien!");
        } else {
            System.out.print("ERROR: ");
            switch (codigo) {
                case 1:
                    System.out.println("simbolo erroneo");
                    break;
                case 2:
                    System.out.println("se esperaba el punto");
                    break;
                case 3:
                    System.out.println("falta EOF");
                    break;
                case 4:
                    System.out.println("se esperaba un identificador");
                    break;
                case 5:
                    System.out.println("se esperaba '='");
                    break;
                case 6:
                    System.out.println("se esperaba un numero");
                    break;
                case 7:
                    System.out.println("se esperaba ';'");
                    break;
                case 8:
                    System.out.println("se esperaba una asignacion");
                    break;
                case 10:
                    System.out.println("se esperaba 'END'");
                    break;
                case 11:
                    System.out.println("se esperaba 'THEN'");
                    break;
                case 12:
                    System.out.println("se esperaba 'DO'");
                    break;
                case 13:
                    System.out.println("se esperaba un cierre de parentesis");
                    break;
                case 14:
                    System.out.println("proposicion no valida");
                    break;
                case 15:
                    System.out.println("se esperaba un operador de comparacion");
                    break;
                case 16:
                    System.out.println("se esperaba una variable");
                    break;
                case 17:
                    System.out.println("factor no valido");
                    break;
                case 18:
                    System.out.println("se excedio la cantidad de variables a declarar");
                    break;
                case 19:
                    System.out.println("identificador repetido");
                    break;
                case 20:
                    System.out.println("el identificador no fue declarado");
                    break;
                case 21:
                    System.out.println("el procedure no fue declarado");
                    break;
                case 22:
                    System.out.println("se esperaba un procedure");
                    break;
                case 23:
                    System.out.println("se esperaba una apertura de parentesis");
                    break;
                case 24:
                    System.out.println("la variable no fue declarada");
                    break;
                case 25:
                    System.out.println("se esperaba una constante o una variable");
                    break;
                case 26:
                    System.out.println("se esperaba un identificador o un numero");
                    break;
                case 27:
                    System.out.println("el simbolo esta de mas");
                    break;
                case 28:
                    System.out.println("se esperaba EOF");
                case 29:
                    System.out.println("se esperaba 'TO'");
                case 30:
                    System.out.println("se esperaba 'TIMES'");
                case 31:
                    System.out.println("se esperaba 'BEGIN'");
            }
        }
        System.exit(codigo);
    }
}
