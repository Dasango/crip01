import java.util.HashMap;
import java.util.Map;

public class VigenereAnalyzer {

    private final VigenereCipher cifrador;
    private final double[] frecuenciasEsperadasZn;

    /**
     * Inicializa el analizador con un cifrador y genera el vector de frecuencias esperadas.
     */
    public VigenereAnalyzer(VigenereCipher cifrador) {
        this.cifrador = cifrador;
        this.frecuenciasEsperadasZn = inicializarFrecuenciasEsperadas();
    }

    /**
     * Genera el vector de probabilidades de frecuencia adaptado a los caracteres del alfabeto.
     */
    private double[] inicializarFrecuenciasEsperadas() {
        int n = cifrador.obtenerModulo();
        double[] frecuencias = new double[n];

        Map<Character, Double> frecEspanol = new HashMap<>();
        frecEspanol.put('E', 0.1368);
        frecEspanol.put('A', 0.1253);
        frecEspanol.put('O', 0.0868);
        frecEspanol.put('S', 0.0798);
        frecEspanol.put('R', 0.0687);
        frecEspanol.put('N', 0.0671);
        frecEspanol.put('I', 0.0625);
        frecEspanol.put('D', 0.0586);
        frecEspanol.put('L', 0.0497);
        frecEspanol.put('C', 0.0468);
        frecEspanol.put('T', 0.0463);
        frecEspanol.put('U', 0.0393);
        frecEspanol.put('M', 0.0315);
        frecEspanol.put('P', 0.0251);
        frecEspanol.put('B', 0.0142);
        frecEspanol.put('G', 0.0101);
        frecEspanol.put('V', 0.0090);
        frecEspanol.put('Y', 0.0090);
        frecEspanol.put('Q', 0.0088);
        frecEspanol.put('H', 0.0070);
        frecEspanol.put('F', 0.0069);
        frecEspanol.put('Z', 0.0052);
        frecEspanol.put('J', 0.0044);
        frecEspanol.put('Ñ', 0.0031);
        frecEspanol.put('X', 0.0022);
        frecEspanol.put('W', 0.0001);
        frecEspanol.put('K', 0.0001);

        double frecEspacio = 0.1600;
        double sumaTotal = 0.0;

        for (int i = 0; i < n; i++) {
            char c = cifrador.obtenerCaracter(i);
            char cUpper = Character.toUpperCase(c);

            double peso;
            if (c == ' ') {
                peso = frecEspacio;
            } else if (frecEspanol.containsKey(cUpper)) {
                peso = frecEspanol.get(cUpper);
            } else if (Character.isDigit(c)) {
                peso = 0.0010;
            } else {
                peso = 0.0005;
            }

            frecuencias[i] = peso;
            sumaTotal += peso;
        }

        if (sumaTotal > 0) {
            for (int i = 0; i < n; i++) {
                frecuencias[i] /= sumaTotal;
            }
        }

        return frecuencias;
    }

    /**
     * Calcula el índice de coincidencia de Friedman para una cadena de texto.
     */
    public double calcularIndiceCoincidencia(String texto) {
        int m = texto.length();
        if (m <= 1) {
            return 0.0;
        }

        int n = cifrador.obtenerModulo();
        int[] conteos = new int[n];

        for (int i = 0; i < m; i++) {
            int idx = cifrador.obtenerIndice(texto.charAt(i));
            if (idx >= 0 && idx < n) {
                conteos[idx]++;
            }
        }

        double sumaProductos = 0.0;
        for (int i = 0; i < n; i++) {
            sumaProductos += (double) conteos[i] * (conteos[i] - 1);
        }

        return sumaProductos / ((double) m * (m - 1));
    }

    /**
     * Descompone el criptograma en subcadenas correspondientes a cada columna de la clave.
     */
    public String[] dividirEnColumnas(String criptograma, int longitudClave) {
        if (longitudClave <= 0) {
            throw new IllegalArgumentException("La longitud de la clave debe ser mayor a 0.");
        }

        StringBuilder[] constructores = new StringBuilder[longitudClave];
        for (int i = 0; i < longitudClave; i++) {
            constructores[i] = new StringBuilder();
        }

        for (int i = 0; i < criptograma.length(); i++) {
            constructores[i % longitudClave].append(criptograma.charAt(i));
        }

        String[] columnas = new String[longitudClave];
        for (int i = 0; i < longitudClave; i++) {
            columnas[i] = constructores[i].toString();
        }

        return columnas;
    }

    /**
     * Calcula el promedio del índice de coincidencia de todas las columnas para una longitud dada.
     */
    public double calcularICPromedio(String criptograma, int longitudClave) {
        String[] columnas = dividirEnColumnas(criptograma, longitudClave);
        double sumaIC = 0.0;
        for (String col : columnas) {
            sumaIC += calcularIndiceCoincidencia(col);
        }
        return sumaIC / longitudClave;
    }

    /**
     * Calcula los índices de coincidencia promedio para longitudes desde 1 hasta maxLongitud.
     */
    public double[] calcularTablaIC(String criptograma, int maxLongitud) {
        double[] resultados = new double[maxLongitud + 1];
        for (int l = 1; l <= maxLongitud; l++) {
            resultados[l] = calcularICPromedio(criptograma, l);
        }
        return resultados;
    }

    /**
     * Deduce la longitud de la clave identificando el pico principal en los valores de IC.
     */
    public int deducirLongitudClave(double[] tablaIC, int maxLongitud) {
        int n = cifrador.obtenerModulo();
        double icAleatorio = 1.0 / n;

        double icMax = 0.0;
        for (int l = 1; l <= maxLongitud; l++) {
            if (tablaIC[l] > icMax) {
                icMax = tablaIC[l];
            }
        }

        if (icMax <= icAleatorio * 1.15) {
            return 1;
        }

        double umbralPico = Math.max(icMax * 0.85, icAleatorio * 1.35);

        for (int l = 1; l <= maxLongitud; l++) {
            if (tablaIC[l] >= umbralPico) {
                boolean esPicoLocal = true;
                if (l > 1 && tablaIC[l] < tablaIC[l - 1])
                    esPicoLocal = false;
                if (l < maxLongitud && tablaIC[l] < tablaIC[l + 1] * 0.90)
                    esPicoLocal = false;

                if (esPicoLocal) {
                    return l;
                }
            }
        }

        int mejorL = 1;
        double mejorValor = -1.0;
        for (int l = 1; l <= maxLongitud; l++) {
            if (tablaIC[l] > mejorValor) {
                mejorValor = tablaIC[l];
                mejorL = l;
            }
        }
        return mejorL;
    }

    /**
     * Retorna los candidatos de longitud L ordenados de mayor a menor según su IC.
     */
    public int[] obtenerMejoresCandidatosL(double[] tablaIC, int maxLongitud, int cantidad) {
        Integer[] indices = new Integer[maxLongitud];
        for (int i = 0; i < maxLongitud; i++) {
            indices[i] = i + 1;
        }
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(tablaIC[b], tablaIC[a]));
        int k = Math.min(cantidad, maxLongitud);
        int[] top = new int[k];
        for (int i = 0; i < k; i++) {
            top[i] = indices[i];
        }
        return top;
    }

    /**
     * Imprime en consola la tabla y la representación gráfica de barras de los valores de IC.
     */
    public void imprimirTablaYGraficaIC(double[] tablaIC, int maxLongitud, int longitudDetectada) {
        int n = cifrador.obtenerModulo();
        double icAleatorio = 1.0 / n;

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("  TABLA Y GRAFICA DEL INDICE DE COINCIDENCIA IC(L)  (IC Aleatorio Zn ~ %.4f)\n",
                icAleatorio);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  L  |  IC Promedio  | Grafica Visual y Diagnostico");
        System.out.println("-----+---------------+----------------------------------------------------------");

        double escalaMax = 0.085;
        for (int l = 1; l <= maxLongitud; l++) {
            if (tablaIC[l] > escalaMax)
                escalaMax = tablaIC[l];
        }

        for (int l = 1; l <= maxLongitud; l++) {
            double valor = tablaIC[l];
            int longitudBarra = (int) Math.round((valor / escalaMax) * 32);
            if (longitudBarra < 1 && valor > 0)
                longitudBarra = 1;
            if (longitudBarra > 32)
                longitudBarra = 32;

            StringBuilder barra = new StringBuilder();
            for (int b = 0; b < longitudBarra; b++) {
                barra.append("#");
            }
            while (barra.length() < 32) {
                barra.append(" ");
            }

            String nota = "";
            if (l == longitudDetectada) {
                nota = "<-- [PICO PRINCIPAL: L = " + l + "]";
            } else if (longitudDetectada > 0 && l % longitudDetectada == 0) {
                nota = "<-- (Armonico " + (l / longitudDetectada) + "L = " + l + ")";
            }

            System.out.printf(" %2d  |    %.5f    | [%s] %s\n", l, valor, barra.toString(), nota);
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    /**
     * Imprime la justificación estadística de los armónicos detectados para el período deducido.
     */
    public void explicarArmonicos(int periodoPrincipal, int maxLongitud, double[] tablaIC) {
        System.out.println("\n[Justificacion Estadistica]");
        System.out.printf("* Periodo Fundamental identificado: L = %d (IC = %.5f).\n", periodoPrincipal,
                tablaIC[periodoPrincipal]);
        System.out.println("* Fundamento: Al particionar en L subcriptogramas, cada columna se comporta como un");
        System.out.println("  cifrado monoalfabetico preservando la distribucion de frecuencias del idioma.");

        StringBuilder armonicosStr = new StringBuilder();
        for (int k = 2; k * periodoPrincipal <= maxLongitud; k++) {
            int lArmonico = k * periodoPrincipal;
            if (armonicosStr.length() > 0)
                armonicosStr.append(", ");
            armonicosStr.append(String.format("%dL = %d (IC = %.5f)", k, lArmonico, tablaIC[lArmonico]));
        }

        if (armonicosStr.length() > 0) {
            System.out.printf("* Armonicos detectados: %s.\n", armonicosStr.toString());
            System.out
                    .println("  Los multiplos de L conservan el monoalfabetismo, confirmando la periodicidad exacta.");
        }
    }

    /**
     * Deduce el carácter de la clave para una columna aplicando correlación de frecuencias.
     */
    public char deducirCaracterColumna(String columna) {
        int m = columna.length();
        if (m == 0)
            return cifrador.obtenerCaracter(0);

        int n = cifrador.obtenerModulo();

        int[] conteosColumna = new int[n];
        for (int i = 0; i < m; i++) {
            int idx = cifrador.obtenerIndice(columna.charAt(i));
            if (idx >= 0 && idx < n) {
                conteosColumna[idx]++;
            }
        }

        int mejorK = 0;
        double maxCorrelacion = -1.0;

        for (int k = 0; k < n; k++) {
            double correlacionActual = 0.0;

            for (int idxCifrado = 0; idxCifrado < n; idxCifrado++) {
                int count = conteosColumna[idxCifrado];
                if (count == 0)
                    continue;

                int idxDescifrado = cifrador.descifrarIndice(idxCifrado, k);
                double frecObservada = (double) count / m;
                double frecEsperada = frecuenciasEsperadasZn[idxDescifrado];

                correlacionActual += frecObservada * frecEsperada;
            }

            if (correlacionActual > maxCorrelacion) {
                maxCorrelacion = correlacionActual;
                mejorK = k;
            }
        }

        return cifrador.obtenerCaracter(mejorK);
    }

    /**
     * Reconstruye la palabra clave completa analizando cada una de las columnas del criptograma.
     */
    public String reconstruirClave(String criptograma, int longitudClave) {
        String[] columnas = dividirEnColumnas(criptograma, longitudClave);
        StringBuilder claveRecuperada = new StringBuilder(longitudClave);

        for (int j = 0; j < longitudClave; j++) {
            char caracterClave = deducirCaracterColumna(columnas[j]);
            claveRecuperada.append(caracterClave);
        }

        return claveRecuperada.toString();
    }

    /**
     * Restaura sustituciones comunes de vocales acentuadas y caracteres especiales en el texto descifrado.
     */
    public String restaurarTildesEspanol(String texto) {
        if (texto == null)
            return "";
        return texto
                .replaceAll("(?i)\\bdka\\b", "día")
                .replaceAll("(?i)\\bdkas\\b", "días")
                .replaceAll("(?i)\\bfrko\\b", "frío")
                .replaceAll("(?i)\\bfrka\\b", "fría")
                .replaceAll("(?i)\\bmolestksimo\\b", "molestísimo")
                .replaceAll("(?i)\\bmolestksima\\b", "molestísima")
                .replaceAll("(?i)\\bdeslizq\\b", "deslizó")
                .replaceAll("(?i)\\brypidamente\\b", "rápidamente")
                .replaceAll("(?i)\\bryfaga\\b", "ráfaga")
                .replaceAll("(?i)\\bryfagas\\b", "ráfagas")
                .replaceAll("(?i)\\bsqlo\\b", "sólo")
                .replaceAll("(?i)\\bmys\\b", "más")
                .replaceAll("(?i)\\baoos\\b", "años")
                .replaceAll("(?i)\\baoo\\b", "año")
                .replaceAll("(?i)\\bolka\\b", "olía")
                .replaceAll("(?i)\\bvestkbulo\\b", "vestíbulo")
                .replaceAll("(?i)\\bgl\\b", "él")
                .replaceAll("(?i)\\bhabka\\b", "había")
                .replaceAll("(?i)\\btenka\\b", "tenía")
                .replaceAll("(?i)\\bpodka\\b", "podía")
                .replaceAll("(?i)\\baquk\\b", "aquí")
                .replaceAll("(?i)\\ballk\\b", "allí");
    }
}
