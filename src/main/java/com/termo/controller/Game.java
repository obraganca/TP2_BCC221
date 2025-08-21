package com.termo.controller;

import com.termo.model.DataSourceModel;

import java.text.Normalizer;
import java.util.Map;
import java.util.HashMap;

public class Game {
    private String palavratentativa;
    private int rightQuantityWord=0;
    private char[] coresresultado = new char[5]; // o vetor de char coresresultado tem os valores G Y ou B em cada uma das posições armazenando um sinal para a cor de cada letra da tentativa atual
    private DataSourceModel dataSourceModel;
    public Game() {
        this.dataSourceModel = new DataSourceModel();
        System.out.println(this.dataSourceModel.getWord());
    }

    public boolean validateGuess(String chute) {
        System.out.println("Chute: " + chute);
        rightQuantityWord = 0;

        if (chute.length() != getWordLength()) {
            System.out.println("Tentativa inválida: precisa de " + getWordLength() + " letras.");
            return false;
        }

        String chuteLower = chute.toLowerCase();

        // verifica existência (considerando normalização)
        if (!dataSourceModel.searchWord(chuteLower)) {
            return false;
        }

        // obtém a forma canonical (a palavra do dicionário, possivelmente acentuada)
        String canonical = dataSourceModel.getCanonicalWord(chuteLower);
        if (canonical == null) {
            // fallback para o chute digitado
            canonical = chuteLower;
        }

        // armazena a palavra tentativa com acento (para exibir nas LetterBox)
        this.palavratentativa = canonical.toUpperCase();

        // Normaliza (remove acentos e cedilha) para comparação
        String chuteNorm = normalize(canonical).toUpperCase(); // importante: normaliza a versão canonical
        String secret = dataSourceModel.getWord();
        String secretNorm = normalize(secret).toUpperCase();

        Map<Character, Integer> contagem = new HashMap<>();

        // Conta ocorrências na palavra secreta (normalizada)
        for (char c : secretNorm.toCharArray()) {
            contagem.put(c, contagem.getOrDefault(c, 0) + 1);
        }

        // Primeiro passe: verdes (posições corretas)
        for (int i = 0; i < secretNorm.length(); i++) {
            if (chuteNorm.charAt(i) == secretNorm.charAt(i)) {
                coresresultado[i] = 'G';
                rightQuantityWord++;
                contagem.put(chuteNorm.charAt(i), contagem.get(chuteNorm.charAt(i)) - 1);
            } else {
                coresresultado[i] = 'B';
            }
        }

        // Segundo passe: amarelos (letra existe em outra posição)
        for (int i = 0; i < chuteNorm.length(); i++) {
            if (coresresultado[i] == 'G') continue;
            char ch = chuteNorm.charAt(i);
            if (contagem.getOrDefault(ch, 0) > 0) {
                coresresultado[i] = 'Y';
                contagem.put(ch, contagem.get(ch) - 1);
            } else {
                coresresultado[i] = 'B';
            }
        }

        return true;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        // remove marcas diacríticas (acentos) — ç também vira c
        n = n.replaceAll("\\p{M}", "");
        return n;
    }




    public int getWordLength(){
        return this.dataSourceModel.getWord().length();
    }

    public int getRightQuantityWord() {
        return rightQuantityWord;
    }

    public void setRightQuantityWord(int rightQuantityWord) {
        this.rightQuantityWord = rightQuantityWord;
    }
    public String getpalavratentativa(){
        return this.palavratentativa;
    }
    public String getPalavra() {
        return this.dataSourceModel.getWord();
    }
    public char[] getResultado(){
        return coresresultado;
    }
}