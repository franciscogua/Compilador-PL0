package compilador;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import static compilador.Terminal.*;
import static compilador.Constantes.*;

public class GeneradorDeCodigo {

    private final String nombreArch;
    private final ArrayList<Byte> memoria;
    private final EnumMap<Terminal, String> hexaByOperador;

    public GeneradorDeCodigo(String nomArch) {

        this.nombreArch = nomArch;
        memoria = new ArrayList<>();

        hexaByOperador = new EnumMap<>(Terminal.class);

        setupHexaByOperador();
    }

    public void cargarParteTamFijo() {
        String header = "4D5A60010100000004000000FFFF00006001000000000000400000000000000000000000000000000000000000000000000000000000000000000000A00000000E1FBA0E00B409CD21B8014CCD21546869732070726F6772616D20697320612057696E333220636F6E736F6C65206170706C69636174696F6E2E2049742063616E6E6F742062652072756E20756E646572204D532D444F532E0D0A2400000000504500004C0101000000534C0000000000000000E00002010B010100000800000000000000000000001500000010000000200000000040000010000000020000040000000000000004000000000000000020000000020000000000000300000000001000001000000000100000100000000000001000000000000000000000001C100000280000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000001C0000000000000000000000000000000000000000000000000000002E746578740000000C060000001000000008000000020000000000000000000000000000200000E0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000006E1000007C1000008C10000098100000A4100000B610000000000000521000000000000000000000441000000010000000000000000000000000000000000000000000004B45524E454C33322E646C6C00006E1000007C1000008C10000098100000A4100000B61000000000000000004578697450726F6365737300000047657453746448616E646C65000000005265616446696C6500000000577269746546696C65000000476574436F6E736F6C654D6F646500000000536574436F6E736F6C654D6F64650000000000000000000050A21C11400031C003052C114000750D6AF5FF1504104000A32C1140006A0068301140006A01681C11400050FF150C10400009C075086A00FF1500104000813D301140000100000075EC58C3005772697465206572726F720000000000000000000000000000000000000000000000006031C00305CC11400075376AF6FF1504104000A3CC11400068D011400050FF15101040008025D0114000F9FF35D0114000FF35CC114000FF1514104000A1CC1140006A0068D41140006A0168BE11400050FF150810400009C0619075086A00FF15001040000FB605BE114000813DD4114000010000007405B8FFFFFFFFC30052656164206572726F7200000000000000000000000000000000000000000000006089C630C00206740846E8E1FEFFFFEBF26190C30000000000000000000000000430E8C9FEFFFFC30000000000000000B00DE8B9FEFFFFB00AE8B2FEFFFFC3003D00000080754EB02DE8A2FEFFFFB002E8CBFFFFFFB001E8C4FFFFFFB004E8BDFFFFFFB007E8B6FFFFFFB004E8AFFFFFFFB008E8A8FFFFFFB003E8A1FFFFFFB006E89AFFFFFFB004E893FFFFFFB008E88CFFFFFFC33D000000007D0B50B02DE84CFEFFFF58F7D83D0A0000000F8CEF0000003D640000000F8CD10000003DE80300000F8CB30000003D102700000F8C950000003DA08601007C7B3D40420F007C613D809698007C473D00E1F5057C2D3D00CA9A3B7C13BA00000000BB00CA9A3BF7FB52E818FFFFFF58BA00000000BB00E1F505F7FB52E805FFFFFF58BA00000000BB80969800F7FB52E8F2FEFFFF58BA00000000BB40420F00F7FB52E8DFFEFFFF58BA00000000BBA0860100F7FB52E8CCFEFFFF58BA00000000BB10270000F7FB52E8B9FEFFFF58BA00000000BBE8030000F7FB52E8A6FEFFFF58BA00000000BB64000000F7FB52E893FEFFFF58BA00000000BB0A000000F7FB52E880FEFFFF58E87AFEFFFFC300FF15001040000000B900000000B3035153E8A2FDFFFF5B593C0D0F84340100003C080F84940000003C2D0F84090100003C307CDB3C397FD72C3080FB0074D080FB02750C81F90000000075043C0074BF80FB03750A3C007504B300EB02B30181F9CCCCCC0C7FA881F9343333F37CA088C7B80A000000F7E93D0800008074113DF8FFFF7F751380FF077E0EE97FFFFFFF80FF080F8F76FFFFFFB90000000088F980FB02740401C1EB0329C89188F85153E8C3FDFFFF5B59E953FFFFFF80FB030F844AFFFFFF5153B008E87AFCFFFFB020E873FCFFFFB008E86CFCFFFF5B5980FB007507B303E925FFFFFF80FB02750F81F9000000007507B303E911FFFFFF89C8B90A000000BA000000003D000000007D08F7D8F7F9F7D8EB02F7F989C181F9000000000F85E6FEFFFF80FB020F84DDFEFFFFB303E9D6FEFFFF80FB030F85CDFEFFFFB02D5153E8FDFBFFFF5B59B302E9BBFEFFFF80FB030F84B2FEFFFF80FB02750C81F9000000000F84A1FEFFFF51E814FDFFFF5989C8C3";
        Byte[] headerBytes = ByteUtil.hexaToByteArray(header);
        cargarByteArray(headerBytes);
        cargarBytes("BF00000000"); // MOV EDI
    }

    public void reemplazarInicio() {
        int posVar = traerTop()
                + traerIntdeBytes(POSICION_CODIGO_BASE)// 0x1000
                + traerIntdeBytes(POSICION_IMAGEN_BASE)// 0x400000
                - traerIntdeBytes(POSICION_TAMANIO_HEADER);// 0x200

        reemplazarInt(POSICION_MOV_EDI + 1, posVar);
    }

    public void iniciarVars(int contador) {
        for (int i = 0; i < contador; i++) {
            cargarBytes("00000000");
        }
    }

    public void reemplazarTamanoVirtual() {
        int tamanoTexto = traerTop()
                - traerIntdeBytes(POSICION_TAMANIO_HEADER);// 0x200

        reemplazarInt(POSICION_TAMANIO_VIRTUAL, tamanoTexto);
    }

    public void llenarCeros() {
        int aa = traerIntdeBytes(POSICION_ALINEAMIENTO_ARCHIVO);
        while (traerTop() % aa != 0) {
            cargarByte("00");
        }
    }

    public void reemplazarTamanoSeccCod() {
        int tamanoTexto = traerTop()
                - traerIntdeBytes(POSICION_TAMANIO_HEADER);// 0x200

        reemplazarInt(POSICION_SECCION_CODIGO, tamanoTexto);
    }

    public void reemplazarDataPura() {
        int tamanoTexto = traerTop()
                - traerIntdeBytes(POSICION_TAMANIO_HEADER);// 0x200

        reemplazarInt(POSICION_DATA_PURA, tamanoTexto);
    }

    public void reemplazarTamanoImgyBaseDato() {
        int tamanoCodSecc = traerIntdeBytes(POSICION_SECCION_CODIGO);
        int tamanoDataPura = traerIntdeBytes(POSICION_DATA_PURA);
        int alinSecc = traerIntdeBytes(POSICION_ALINEAMIENTO_SECCION);

        int nuevaVar = (2 + tamanoCodSecc / alinSecc) * alinSecc;
        reemplazarInt(POSICION_TAMANIO_IMAGEN, nuevaVar);

        int nuevaVar2 = (2 + tamanoDataPura / alinSecc) * alinSecc;
        reemplazarInt(POSICION_BASE_DATOS, nuevaVar2);
    }

    public void generarFactorVar(int valor) {
        cargarBytes("8B87");// MOV EAX
        cargarIntegerComoBytes(valor);// valor
        cargarPushEAX();// PUSH EAX
    }

    public void generarFactorNum(int valor) {
        cargarByte("B8"); // MOV EAX
        cargarIntegerComoBytes(valor); // valor
        cargarPushEAX();// PUSH EAX
    }

    public void cargarAsignacion(int valor) {
        cargarPopEAX(); // POP EAX
        cargarBytes("8987"); // MOV [EDI+valor], EAX
        cargarIntegerComoBytes(valor);
    }

    public void cargarReadln(int valor) {
        int distancia = traerDistancia(traerTop(), POSICION_MEMORIA_READLN);
        cargarByte("E8"); // CALL dir
        cargarIntegerComoBytes(distancia);
        cargarBytes("8987"); // MOV [EDI+valor], EAX
        cargarIntegerComoBytes(valor); // valor
    }

    public void cargarCall(int valor) {
        int distancia = traerDistancia(traerTop(), valor);
        cargarByte("E8");
        cargarIntegerComoBytes(distancia);// valor
    }

    public void reemplazarInt(int pos, int a) {
        int revInt = ByteUtil.revertirInteger(a);
        Byte[] b = ByteUtil.intAByteArray(revInt);
        for (int i = 0; i < 4; i++) {
            memoria.set(pos + i, b[i]);
        }
    }

    public void cargarSalto(int saltoDistancia) {
        cargarByte("E9");
        cargarIntegerComoBytes(saltoDistancia);
    }

    public void impar() {
        cargarPopEAX(); // POP EAX
        cargarBytes("A801"); // TEST AL, ab
        cargarBytes("7B05"); // JPO dir
        cargarBytes("E900000000"); // JMP dir
    }

    public void cargarCondicion(Terminal simbolo) {

        String hexa = hexaByOperador.get(simbolo);

        cargarPopEAX(); // POP EAX
        cargarByte("5B"); // POP EBX
        cargarBytes("39C3"); // CMP EBX, EAX
        cargarBytes(hexa);
        cargarBytes("E900000000"); // JMP dir
    }

    public void suma() {
        cargarPopEAX(); // POP EAX
        cargarByte("5B"); // POP EBX
        cargarBytes("01D8"); // ADD EAX, EBX
        cargarPushEAX();// PUSH EAX
    }

    public void resta() {
        cargarPopEAX(); // POP EAX
        cargarByte("5B"); // POP EBX
        cargarByte("93"); // XCHG EAX, EBX
        cargarBytes("29D8"); // SUB EAX, EBX
        cargarPushEAX(); // PUSH EAX
    }

    public void menosUnario() {
        cargarPopEAX(); // POP EAX
        cargarBytes("F7D8"); // NEG EAX
        cargarPushEAX(); // PUSH EAX
    }

    public void multiplicar() {
        cargarPopEAX(); // POP EAX
        cargarByte("5B"); // POP EBX
        cargarBytes("F7EB"); // IMUL EBX
        cargarPushEAX(); // PUSH EAX
    }

    public void dividir() {
        cargarPopEAX(); // POP EAX
        cargarByte("5B"); // POP EBX
        cargarByte("93"); // XCHG EAX, EBX
        cargarByte("99"); //CDQ
        cargarBytes("F7FB"); // IDIV EBX
        cargarPushEAX(); // PUSH EAX
    }

    public void cargarBloqueFinalProcedure() {
        cargarByte("C3");
    }

    public void cargarFinalDelPrograma() {
        int distancia = traerDistancia(traerTop(), POSICION_FINAL_DEL_PROGRAMA);
        cargarByte("E9"); // CALL dir
        cargarIntegerComoBytes(distancia);
    }

    public void cargarPopEAX() {
        if (memoria.get(traerTop() - 1) == 50) {
            memoria.remove(traerTop() - 1);
        } else {
            cargarByte("58"); // POP EAX
        }
    }

    private void cargarPushEAX() {
        cargarByte("50"); // PUSH EAX
    }

    public void cargarSalidaExpresion() {
        cargarPopEAX();
        int distancia = traerDistancia(traerTop(), MUESTRA_INT_EAX);
        cargarByte("E8"); // CALL dir
        cargarIntegerComoBytes(distancia);
    }

    public void cargarWriteString(String cad) {

        int strPos = traerTop()
                + DESPLAZAMIENTO_STRING // 15
                + traerIntdeBytes(POSICION_CODIGO_BASE) // 0x1000
                + traerIntdeBytes(POSICION_IMAGEN_BASE) // 0x400000
                - traerIntdeBytes(POSICION_TAMANIO_HEADER); // 0x200

        cargarByte("B8"); // MOV EAX
        cargarIntegerComoBytes(strPos); // Posicion de la cadena

        int distancia = traerDistancia(traerTop(), MUESTRA_CADENA);
        cargarByte("E8"); //CALL dir
        cargarIntegerComoBytes(distancia);

        cargarByte("E9"); // Salto E9 00 00 00
        cargarIntegerComoBytes(cad.length() - 1);

        // Genera los bytes de la cadena
        for (int i = 1; i < (cad.length() - 1); i++) {
            memoria.add((byte) cad.charAt(i));
        }

        cargarByte("00");
    }

    private void cargarByte(Byte valor) {
        memoria.add(valor);
    }

    private void cargarByteArray(Byte[] valor) {
        memoria.addAll(Arrays.asList(valor));
    }

    public int traerTop() {
        return memoria.size();
    }

    public void volcarMemoria() throws IOException {

        FileOutputStream fos = new FileOutputStream(nombreArch);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        try (DataOutputStream dos = new DataOutputStream(bos)) {
            for (Byte aByte : memoria) {
                dos.writeByte(aByte);
            }
        }
    }

    private void cargarBytes(String valor) {
        Byte[] byteArray = ByteUtil.hexaToByteArray(valor);
        cargarByteArray(byteArray);
    }

    public void cargarByte(String valor) {
        if (valor.length() != 2) {
            throw new IllegalArgumentException("El largo del parametro debe ser 2 y se obtuvo: " + valor.length());
        } else {
            Byte b = ByteUtil.hexaToByte(valor);
            cargarByte(b);
        }
    }

    public void cargarByteComoInteger(String valor) {

        int integer = Integer.parseInt(valor);
        integer = ByteUtil.revertirInteger(integer);
        Byte b = ByteUtil.intToByte(integer);
        cargarByte(b);
    }

    private void setupHexaByOperador() {
        hexaByOperador.put(IGUAL, "7405");
        hexaByOperador.put(DISTINTO, "7505");
        hexaByOperador.put(MENOR, "7C05");
        hexaByOperador.put(MENOR_IGUAL, "7E05");
        hexaByOperador.put(MAYOR, "7F05");
        hexaByOperador.put(MAYOR_IGUAL, "7D05");
    }

    public void cargarIntegerComoBytes(int valor) {
        int revInt = ByteUtil.revertirInteger(valor);
        Byte[] b = ByteUtil.intAByteArray(revInt);
        cargarByteArray(b);
    }

    public int traerIntdeBytes(int p) {
        int resultado = memoria.get(p)
                + memoria.get(p + 1) * 256
                + memoria.get(p + 2) * (256 * 256)
                + memoria.get(p + 3) * (256 * 256 * 256);
        return resultado;
    }

    private int traerDistancia(int origen, int destino) {
        return destino - (origen + 5);
    }

    public void cargarFor(int dirVar, int hasta) {
        generarFactorVar(dirVar); // Direccionamiento indexado
        generarFactorNum(hasta); // Direccionamiento inmediato
        cargarCondicion(MENOR_IGUAL);
    }

    public void cargarRepeat() {
        cargarByte("B8");
        cargarIntegerComoBytes(1);
        cargarPushEAX();
        resta();

        cargarByte("B8");
        cargarIntegerComoBytes(0);

        cargarByte("93");
        cargarPopEAX();
        cargarPushEAX();
        cargarBytes("39C3"); // CMP EBX, EAX
        cargarBytes("7E05"); // CMP EBX, EAX

        cargarSalto(0);
    }

    public void incrementar(int dirVar) {
        int incremental = 1;

        generarFactorVar(dirVar);
        generarFactorNum(incremental);
        suma();
        cargarAsignacion(dirVar);
    }

    public void cargarHalt() {
        int distancia = Constantes.FINALIZA_PROGRAMA - (this.traerTop() + 5);
        cargarSalto(distancia);
    }

    public void cargarSqr() {
        cargarByte("5B"); // POP EBX
        cargarBytes("F7EB"); // IMUL EBX
        cargarPushEAX();
    }

}
