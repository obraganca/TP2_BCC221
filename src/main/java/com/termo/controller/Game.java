package com.termo.controller;

import com.termo.model.DataSourceModel;
import java.text.Normalizer;
import java.util.Map;
import java.util.HashMap;

/**
 * Classe responsável pela lógica principal do jogo Termo.
 * Faz a validação dos chutes do jogador, comparando-os com a palavra secreta
 * e determinando o resultado (letra correta, posição correta ou não existe).
 */
public class Game {
    String file;
    private String palavratentativa; // Armazena a palavra da última tentativa (com acento/canônica)
    private int rightQuantityWord = 0; // Contador de letras corretas na posição correta
    // Vetor de resultado: 'G' = verde, 'Y' = amarelo, 'B' = cinza
    private char[] coresresultado = new char[5];
    private DataSourceModel dataSourceModel;

    /**
     * Construtor da classe Game.
     * Inicializa o modelo de dados e seleciona a palavra secreta.
     *
     * @param file Caminho do arquivo com as palavras possíveis do jogo.
     */
    public Game(String file) {
        this.file = file;
        this.dataSourceModel = new DataSourceModel(file);
        System.out.println(this.dataSourceModel.getWord()); // Debug: imprime a palavra sorteada
    }

    /**
     * validateGuess
     * Valida e processa o chute do jogador.
     * Regras:
     * - Verifica se o tamanho é igual ao da palavra secreta
     * - Verifica se a palavra existe no dicionário
     * - Marca letras como 'G' (verde), 'Y' (amarelo) ou 'B' (cinza)
     *
     * @param chute Palavra digitada pelo jogador
     * @return true se o chute for válido e processado, false caso contrário
     */
    public boolean validateGuess(String chute) {
        System.out.println("Chute: " + chute);
        rightQuantityWord = 0;

        // Valida se a palavra tem o mesmo tamanho que a palavra secreta
        if (chute.length() != getWordLength()) {
            System.out.println("Tentativa inválida: precisa de " + getWordLength() + " letras.");
            return false;
        }

        String chuteLower = chute.toLowerCase();

        // Verifica se a palavra existe no dicionário
        if (!dataSourceModel.searchWord(chuteLower)) {
            return false;
        }

        // Obtém a forma canônica (com acentos, caso exista no dicionário)
        String canonical = dataSourceModel.getCanonicalWord(chuteLower);
        if (canonical == null) {
            canonical = chuteLower; // fallback para o próprio chute
        }

        // Armazena a tentativa em maiúsculo (para exibir no jogo)
        this.palavratentativa = canonical.toUpperCase();

        // Normaliza palavras (remove acentos e cedilha) para comparação
        String chuteNorm = normalize(canonical).toUpperCase();
        String secret = dataSourceModel.getWord();
        String secretNorm = normalize(secret).toUpperCase();

        // Mapa com contagem de ocorrências de cada letra na palavra secreta
        Map<Character, Integer> contagem = new HashMap<>();
        for (char c : secretNorm.toCharArray()) {
            contagem.put(c, contagem.getOrDefault(c, 0) + 1);
        }

        // Primeiro passe: marca letras corretas na posição correta (verde)
        for (int i = 0; i < secretNorm.length(); i++) {
            if (chuteNorm.charAt(i) == secretNorm.charAt(i)) {
                coresresultado[i] = 'G';
                rightQuantityWord++;
                // Decrementa ocorrência, pois essa letra já foi usada
                contagem.put(chuteNorm.charAt(i), contagem.get(chuteNorm.charAt(i)) - 1);
            } else {
                coresresultado[i] = 'B'; // provisoriamente cinza
            }
        }

        // Segundo passe: marca letras presentes mas em posição errada (amarelo)
        for (int i = 0; i < chuteNorm.length(); i++) {
            if (coresresultado[i] == 'G') continue; // já marcada como verde
            char ch = chuteNorm.charAt(i);
            if (contagem.getOrDefault(ch, 0) > 0) {
                coresresultado[i] = 'Y';
                contagem.put(ch, contagem.get(ch) - 1); // reduz a contagem
            } else {
                coresresultado[i] = 'B'; // mantém cinza
            }
        }

        return true;
    }

    /**
     * normalize
     * Remove acentos e cedilha de uma string, convertendo para uma forma simplificada.
     *
     * @param s String original (pode ser nula)
     * @return String sem acentos ou null
     */
    private String normalize(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", ""); // remove marcas diacríticas (acentos)
        return n;
    }

    /**
     * getWordLength
     * Retorna o tamanho da palavra secreta.
     *
     * @return Número de caracteres da palavra secreta
     */
    public int getWordLength(){
        return this.dataSourceModel.getWord().length();
    }

    /**
     * getRightQuantityWord
     * @return Quantidade de letras corretas (verdes) no último chute
     */
    public int getRightQuantityWord() {
        return rightQuantityWord;
    }

    public void setRightQuantityWord(int rightQuantityWord) {
        this.rightQuantityWord = rightQuantityWord;
    }

    /**
     * getpalavratentativa
     * @return Palavra da última tentativa (em maiúsculo e canônica)
     */
    public String getpalavratentativa(){
        return this.palavratentativa;
    }

    /**
     * getPalavra
     * @return Palavra secreta do jogo
     */
    public String getPalavra() {
        return this.dataSourceModel.getWord();
    }

    /**
     * getResultado
     * @return Vetor de resultado ('G', 'Y', 'B') da última tentativa
     */
    public char[] getResultado(){
        return coresresultado;
    }
}
