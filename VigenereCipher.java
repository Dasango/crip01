import java.util.HashMap;
import java.util.Map;

public class VigenereCipher {

    private final String alfabeto;
    private final int modulo;
    private final Map<Character, Integer> charAIndice;
    private final Map<Integer, Character> indiceAChar;

    /**
     * Inicializa el alfabeto y las estructuras de mapeo directo e inverso.
     */
    public VigenereCipher(String alfabeto) {
        if (alfabeto == null || alfabeto.isEmpty()) {
            throw new IllegalArgumentException("El alfabeto no puede ser nulo ni vacío.");
        }
        alfabeto = alfabeto.replace("\uFEFF", "").replace("\r", "");
        this.alfabeto = alfabeto;
        this.modulo = alfabeto.length();
        this.charAIndice = new HashMap<>(modulo);
        this.indiceAChar = new HashMap<>(modulo);

        for (int i = 0; i < modulo; i++) {
            char c = alfabeto.charAt(i);
            this.charAIndice.put(c, i);
            this.indiceAChar.put(i, c);
        }
    }

    /**
     * Retorna el tamaño del alfabeto actual.
     */
    public int obtenerModulo() {
        return modulo;
    }

    /**
     * Retorna la cadena completa del alfabeto configurado.
     */
    public String obtenerAlfabeto() {
        return alfabeto;
    }

    /**
     * Obtiene el índice numérico correspondiente a un carácter, o -1 si no pertenece al alfabeto.
     */
    public int obtenerIndice(char caracter) {
        Integer idx = charAIndice.get(caracter);
        return (idx != null) ? idx : -1;
    }

    /**
     * Obtiene el carácter correspondiente a un índice numérico en el alfabeto.
     */
    public char obtenerCaracter(int indice) {
        int indiceNormalizado = Math.floorMod(indice, modulo);
        return indiceAChar.get(indiceNormalizado);
    }

    /**
     * Verifica si un carácter pertenece al alfabeto configurado.
     */
    public boolean perteneceAlAlfabeto(char caracter) {
        return charAIndice.containsKey(caracter);
    }

    /**
     * Normaliza un carácter para adaptarlo al alfabeto configurado.
     */
    public char normalizarCaracter(char c) {
        if (perteneceAlAlfabeto(c)) {
            return c;
        }
        char mayus = Character.toUpperCase(c);
        if (perteneceAlAlfabeto(mayus)) {
            return mayus;
        }
        char minus = Character.toLowerCase(c);
        if (perteneceAlAlfabeto(minus)) {
            return minus;
        }
        switch (mayus) {
            case 'Á': if (perteneceAlAlfabeto('A')) return 'A'; break;
            case 'É': if (perteneceAlAlfabeto('E')) return 'E'; break;
            case 'Í': if (perteneceAlAlfabeto('I')) return 'I'; break;
            case 'Ó': if (perteneceAlAlfabeto('O')) return 'O'; break;
            case 'Ú': case 'Ü': if (perteneceAlAlfabeto('U')) return 'U'; break;
        }
        return '\0';
    }

    /**
     * Filtra el texto manteniendo únicamente los caracteres válidos en el alfabeto.
     */
    public String sanitizarTexto(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char normalizado = normalizarCaracter(texto.charAt(i));
            if (normalizado != '\0') {
                sb.append(normalizado);
            }
        }
        return sb.toString();
    }

    /**
     * Calcula el índice cifrado aplicando aritmética modular.
     */
    public int cifrarIndice(int indiceMensaje, int indiceClave) {
        return Math.floorMod(indiceMensaje + indiceClave, modulo);
    }

    /**
     * Calcula el índice descifrado aplicando aritmética modular.
     */
    public int descifrarIndice(int indiceCifrado, int indiceClave) {
        return Math.floorMod(indiceCifrado - indiceClave, modulo);
    }

    /**
     * Cifra un texto en claro utilizando la clave indicada.
     */
    public String cifrar(String textoPlano, String clave) {
        String textoLimpio = sanitizarTexto(textoPlano);
        String claveLimpia = sanitizarTexto(clave);

        if (claveLimpia.isEmpty()) {
            throw new IllegalArgumentException("La clave no contiene caracteres válidos del alfabeto.");
        }

        StringBuilder resultado = new StringBuilder(textoLimpio.length());
        int longitudClave = claveLimpia.length();

        for (int i = 0; i < textoLimpio.length(); i++) {
            int indiceM = obtenerIndice(textoLimpio.charAt(i));
            int indiceK = obtenerIndice(claveLimpia.charAt(i % longitudClave));
            int indiceC = cifrarIndice(indiceM, indiceK);
            resultado.append(obtenerCaracter(indiceC));
        }

        return resultado.toString();
    }

    /**
     * Descifra un criptograma utilizando la clave indicada.
     */
    public String descifrar(String textoCifrado, String clave) {
        String criptoLimpio = sanitizarTexto(textoCifrado);
        String claveLimpia = sanitizarTexto(clave);

        if (claveLimpia.isEmpty()) {
            throw new IllegalArgumentException("La clave no contiene caracteres válidos del alfabeto.");
        }

        StringBuilder resultado = new StringBuilder(criptoLimpio.length());
        int longitudClave = claveLimpia.length();

        for (int i = 0; i < criptoLimpio.length(); i++) {
            int indiceC = obtenerIndice(criptoLimpio.charAt(i));
            int indiceK = obtenerIndice(claveLimpia.charAt(i % longitudClave));
            int indiceM = descifrarIndice(indiceC, indiceK);
            resultado.append(obtenerCaracter(indiceM));
        }

        return resultado.toString();
    }

    /**
     * Descifra el texto conservando espacios, puntuación y mayúsculas o minúsculas originales.
     */
    public String descifrarConservandoFormato(String textoOriginal, String clave) {
        String claveLimpia = sanitizarTexto(clave);
        if (claveLimpia.isEmpty()) {
            throw new IllegalArgumentException("La clave no contiene caracteres válidos del alfabeto.");
        }

        StringBuilder resultado = new StringBuilder(textoOriginal.length());
        int indiceClave = 0;
        int longClave = claveLimpia.length();

        for (int i = 0; i < textoOriginal.length(); i++) {
            char c = textoOriginal.charAt(i);

            if (perteneceAlAlfabeto(c)) {
                int indiceC = obtenerIndice(c);
                int indiceK = obtenerIndice(claveLimpia.charAt(indiceClave % longClave));
                int indiceM = descifrarIndice(indiceC, indiceK);
                resultado.append(obtenerCaracter(indiceM));
                indiceClave++;
            } else {
                char mayus = Character.toUpperCase(c);
                if (perteneceAlAlfabeto(mayus)) {
                    int indiceC = obtenerIndice(mayus);
                    int indiceK = obtenerIndice(claveLimpia.charAt(indiceClave % longClave));
                    int indiceM = descifrarIndice(indiceC, indiceK);
                    char descifradoChar = obtenerCaracter(indiceM);

                    if (Character.isLowerCase(c)) {
                        resultado.append(Character.toLowerCase(descifradoChar));
                    } else {
                        resultado.append(descifradoChar);
                    }
                    indiceClave++;
                } else {
                    resultado.append(c);
                }
            }
        }

        return resultado.toString();
    }
}
