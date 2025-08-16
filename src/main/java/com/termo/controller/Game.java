package com.termo.controller;

import com.termo.model.DataSourceModel;

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
        rightQuantityWord=0;
        if (chute.length() != 5) {
            System.out.println("Tentativa inválida: precisa de 5 letras.");
            return false;
        }
        chute = chute.toLowerCase();
        if(!dataSourceModel.searchWord(chute)){
            return false;
        }
        Map<Character, Integer> contagem = new HashMap<>();

        /* Conta quantas; vezes cada letra aparece na palavra secreta */
        for (char c : dataSourceModel.getWord().toUpperCase().toCharArray()) {
            contagem.put(c, contagem.getOrDefault(c, 0) + 1);
        }
        for (int i = 0; i < dataSourceModel.getWord().length(); i++) {
            if (chute.toUpperCase().charAt(i) == dataSourceModel.getWord().toUpperCase().charAt(i)){
                coresresultado[i] = 'G';
                rightQuantityWord++;
                contagem.put(dataSourceModel.getWord().toUpperCase().charAt(i), contagem.get(chute.toUpperCase().charAt(i)) - 1);
            }else{
                coresresultado[i] = 'B';
            }
        }
        for (int i = 0; i < chute.length(); i++) {
            if (coresresultado[i] == 'G') continue;
            if (contagem.getOrDefault(chute.toUpperCase().charAt(i), 0) > 0) coresresultado[i] = 'Y';
            else
                coresresultado[i] = 'B';
        }
        return true;
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