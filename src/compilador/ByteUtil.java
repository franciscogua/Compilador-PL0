package compilador;

public class ByteUtil {

    public static Byte hexaToByte(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new IllegalArgumentException("La cadena es nula o esta vacia jijiji");
        } else {
            int primerDigito = aDigit(valor.charAt(0));
            int segundoDigito = aDigit(valor.charAt(1));
            Byte b = (byte) ((primerDigito << 4) + segundoDigito);
            return b;
        }
    }

    public static Byte[] hexaToByteArray(String valor) {

        if (valor == null || valor.isEmpty()) {
            //throw new IllegalArgumentException("La cadena es nula o esta vacia jejeje");
            System.out.println("ACÁ ESTÁ EL PROBLEMA");
            return null;
        } else {
            int len = valor.length();
            Byte[] data = new Byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(valor.charAt(i), 16) << 4) + Character.digit(valor.charAt(i + 1), 16));
            }
            return data;
        }
    }

    public static Byte asIntegerToByte(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new IllegalArgumentException("La cadena es nula o esta vacia jujuju");
        } else {
            int integer = Integer.parseInt(valor);
            Byte b = (byte) integer;
            return b;
        }
    }

    public static Byte intToByte(int valor) {
        return (byte) valor;
    }

    public static int revertirInteger(int valor) {
        int b1 = (valor >> 0) & 0xff;
        int b2 = (valor >> 8) & 0xff;
        int b3 = (valor >> 16) & 0xff;
        int b4 = (valor >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }

    public static Byte[] intAByteArray(int valor) {

        Byte[] val = new Byte[4];

        Integer v1 = (valor >> 24) & 0xff;
        Integer v2 = (valor >> 16) & 0xff;
        Integer v3 = (valor >> 8) & 0xff;
        Integer v4 = (valor >> 0) & 0xff;

        val[0] = v1.byteValue();
        val[1] = v2.byteValue();
        val[2] = v3.byteValue();
        val[3] = v4.byteValue();

        return val;
    }

    private static int aDigit(char hexChar) {
        int digito = Character.digit(hexChar, 16);
        if (digito == -1) {
            throw new IllegalArgumentException("Hexadecimal invalido: " + hexChar);
        }
        return digito;
    }
}
