import java.util.Scanner;

public class Main {

    /**
     * Ejecuta el flujo principal de criptoanálisis de Vigenère.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        mostrarCabecera();

        String alfabeto = solicitarAlfabeto(scanner);
        VigenereCipher cifrador = new VigenereCipher(alfabeto);
        VigenereAnalyzer analizador = new VigenereAnalyzer(cifrador);

        String criptogramaOriginal = solicitarCriptograma(scanner);
        String criptogramaSanitizado = cifrador.sanitizarTexto(criptogramaOriginal);

        if (criptogramaSanitizado.isEmpty()) {
            System.out.println("\n[Error] El texto ingresado no contiene caracteres válidos del alfabeto.");
            return;
        }

        long tiempoInicio = System.currentTimeMillis();

        int maxL = Math.min(15, criptogramaSanitizado.length());
        double[] tablaIC = analizador.calcularTablaIC(criptogramaSanitizado, maxL);
        int longitudClave = analizador.deducirLongitudClave(tablaIC, maxL);

        analizador.imprimirTablaYGraficaIC(tablaIC, maxL, longitudClave);
        analizador.explicarArmonicos(longitudClave, maxL, tablaIC);

        String claveRecuperada = analizador.reconstruirClave(criptogramaSanitizado, longitudClave);

        String textoDescifrado = cifrador.descifrarConservandoFormato(criptogramaOriginal, claveRecuperada);
        String textoConTildes = analizador.restaurarTildesEspanol(textoDescifrado);

        long tiempoFin = System.currentTimeMillis();
        double tiempoTotalSegundos = (tiempoFin - tiempoInicio) / 1000.0;

        mostrarResultados(alfabeto, criptogramaSanitizado.length(), longitudClave,
                claveRecuperada, tiempoTotalSegundos, textoDescifrado, textoConTildes);

        permitirAjusteManual(scanner, cifrador, criptogramaOriginal, criptogramaSanitizado,
                analizador, tablaIC, maxL);
    }

    /**
     * Muestra el encabezado del programa en consola.
     */
    private static void mostrarCabecera() {
        System.out.println("================================================================================");
        System.out.println("           CRIPTOANALISIS VIGENERE AUTOMATIZADO SOBRE ANILLOS Zn                ");
        System.out.println("================================================================================");
    }

    /**
     * Solicita y lee el alfabeto ingresado por consola.
     */
    private static String solicitarAlfabeto(Scanner scanner) {
        System.out.println("\nPaso 1: Seleccione o ingrese el alfabeto Zn:");
        System.out.println("  [ENTER]  A-Z estandar (N = 26) [Predeterminado]");
        System.out.println("  [2]      A-Z espanol con Ñ (N = 27)");
        System.out.println("  O ingrese directamente un alfabeto Zn personalizado:");
        System.out.print("> ");
        String entrada = scanner.nextLine().replace("\uFEFF", "").replace("\r", "");

        if (entrada.isEmpty() || entrada.trim().equals("1")) {
            String defaultAlfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            System.out.println("-> Alfabeto cargado: A-Z estandar (Modulo N = 26)");
            return defaultAlfabeto;
        }

        if (entrada.trim().equals("2")) {
            String alfabetoEspanol = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
            System.out.println("-> Alfabeto cargado: A-Z con Ñ (Modulo N = 27)");
            return alfabetoEspanol;
        }

        System.out.println("-> Alfabeto personalizado cargado exitosamente (Modulo N = " + entrada.length() + ")");
        return entrada;
    }

    /**
     * Solicita y lee el texto cifrado ingresado a través de la consola.
     */
    private static String solicitarCriptograma(Scanner scanner) {
        System.out.println("\nPaso 2: Ingrese o pegue el criptograma a continuación.");
        System.out.println("(Pegue el texto y presione ENTER dos veces, o escriba 'FIN' en una línea nueva):");
        System.out.print("> ");

        StringBuilder texto = new StringBuilder();
        while (scanner.hasNextLine()) {
            String linea = scanner.nextLine().replace("\uFEFF", "").replace("\r", "");
            if (linea.trim().equalsIgnoreCase("FIN") || (texto.length() > 0 && linea.trim().isEmpty())) {
                break;
            }
            if (!linea.trim().isEmpty()) {
                if (texto.length() > 0) {
                    texto.append("\n");
                }
                texto.append(linea);
            }
        }
        return texto.toString();
    }

    /**
     * Muestra en pantalla el resumen de resultados del criptoanálisis.
     */
    private static void mostrarResultados(String alfabeto, int longCriptograma, int longitudL,
            String clave, double tiempoSegundos,
            String textoDescifrado, String textoConTildes) {
        System.out.println("\n================================================================================");
        System.out.println("                         RESULTADOS DEL CRIPTOANALISIS                          ");
        System.out.println("================================================================================");
        System.out.println("* Alfabeto Zn configurado    : " + alfabeto + " (N = " + alfabeto.length() + ")");
        System.out.println("* Caracteres criptograficos  : " + longCriptograma);
        System.out.println("* Periodo deducido de clave  : L = " + longitudL);
        System.out.println("* Palabra clave recuperada   : >>> " + clave + " <<<");
        System.out.printf("* Tiempo total de resolucion : %.4f segundos\n", tiempoSegundos);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEXTO DESCIFRADO (CONSERVANDO FORMATO, PUNTUACION Y ESPACIOS):");
        System.out.println("--------------------------------------------------------------------------------");

        int limiteVisual = Math.min(textoDescifrado.length(), 2500);
        System.out.println(textoDescifrado.substring(0, limiteVisual));
        if (textoDescifrado.length() > 2500) {
            System.out
                    .println("\n[... " + (textoDescifrado.length() - 2500) + " caracteres adicionales restantes ...]");
        }

        if (!textoConTildes.equals(textoDescifrado)) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("TEXTO EN ESPAÑOL NATURAL (CON TILDES RESTAURADAS):");
            System.out.println("--------------------------------------------------------------------------------");
            int limiteTildes = Math.min(textoConTildes.length(), 2500);
            System.out.println(textoConTildes.substring(0, limiteTildes));
            if (textoConTildes.length() > 2500) {
                System.out.println(
                        "\n[... " + (textoConTildes.length() - 2500) + " caracteres adicionales restantes ...]");
            }
        }
        System.out.println("================================================================================");
    }

    /**
     * Permite probar periodos L alternativos o claves manuales para ajustar el descifrado.
     */
    private static void permitirAjusteManual(Scanner scanner, VigenereCipher cifrador,
            String criptogramaOriginal, String criptogramaSanitizado,
            VigenereAnalyzer analizador, double[] tablaIC, int maxL) {

        int[] topCandidatos = analizador.obtenerMejoresCandidatosL(tablaIC, maxL, 3);
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  TOP CANDIDATOS DE LONGITUD L SEGUN MAYOR IC:");
        for (int i = 0; i < topCandidatos.length; i++) {
            int lCand = topCandidatos[i];
            System.out.printf("  #%d: L = %-2d  (IC = %.5f)\n", (i + 1), lCand, tablaIC[lCand]);
        }
        System.out.println("--------------------------------------------------------------------------------");

        while (true) {
            System.out.println("\n¿Desea probar otro periodo L o una clave manual?");
            System.out.println("  * Ingrese un valor de L (1 a " + maxL + ") para deducir su clave y descifrar");
            System.out.println("  * Ingrese una palabra clave manual para probarla directamente");
            System.out.println("  * Presione [ENTER] para salir");
            System.out.print("> ");
            String entrada = scanner.nextLine().trim();

            if (entrada.isEmpty()) {
                break;
            }

            String claveProbar;
            if (entrada.matches("\\d+")) {
                int lIngresado = Integer.parseInt(entrada);
                if (lIngresado < 1 || lIngresado > maxL) {
                    System.out.println("[Error] El valor de L debe estar entre 1 y " + maxL + ".");
                    continue;
                }
                claveProbar = analizador.reconstruirClave(criptogramaSanitizado, lIngresado);
                System.out.println("\n-> Clave deducida para L = " + lIngresado + ": >>> " + claveProbar + " <<<");
            } else {
                claveProbar = entrada;
                System.out.println("\n-> Probando clave manual: >>> " + claveProbar + " <<<");
            }

            String descifrado = cifrador.descifrarConservandoFormato(criptogramaOriginal, claveProbar);
            String conTildes = analizador.restaurarTildesEspanol(descifrado);

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("TEXTO DESCIFRADO CON CLAVE [" + claveProbar + "]:");
            System.out.println("--------------------------------------------------------------------------------");
            int limite = Math.min(descifrado.length(), 2500);
            System.out.println(descifrado.substring(0, limite));

            if (!conTildes.equals(descifrado)) {
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("TEXTO CON TILDES RESTAURADAS:");
                System.out.println("--------------------------------------------------------------------------------");
                int limiteTildes = Math.min(conTildes.length(), 2500);
                System.out.println(conTildes.substring(0, limiteTildes));
            }
            System.out.println("--------------------------------------------------------------------------------");
        }

        System.out.println("\nSesion finalizada con exito.");
    }
}
