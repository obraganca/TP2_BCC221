package com.termo.core;

import com.termo.gui.GameWindow;

import java.util.Map;
import java.util.HashMap;

public class Game {
    String palavratentativa;
    char[] coresresultado = new char[5]; // o vetor de char coresresultado tem os valores G Y ou B em cada uma das posições armazenando um sinal para a cor de cada letra da tentativa atual
    String palavra;
    public Game(String palavra) {
        this.palavra = palavra;
    }
    public boolean tentativa(String chute) {
        if (chute.length() != 5) {
            System.out.println("Tentativa inválida: precisa de 5 letras.");
            return false;
        }
        Map<Character, Integer> contagem = new HashMap<>();

        /* Conta quantas; vezes cada letra aparece na palavra secreta */
        for (char c : palavra.toCharArray()) {
            contagem.put(c, contagem.getOrDefault(c, 0) + 1);
        }
        for (int i = 0; i < palavra.length(); i++) {
            if (chute.charAt(i) == palavra.charAt(i)){
                coresresultado[i] = 'G';
                contagem.put(palavra.charAt(i), contagem.get(chute.charAt(i)) - 1);
            }
        }
        for (int i = 0; i < chute.length(); i++) {
            if (coresresultado[i] == 'G') continue;
            if (contagem.getOrDefault(chute.charAt(i), 0) > 0) coresresultado[i] = 'Y';
            else
                coresresultado[i] = 'B';
        }
        for (int i = 0; i < 5; i++){
            System.out.printf("%c",  coresresultado[i]);
        }
        return true;
    }

    //getters e setters
    public void setapalavratentativa(){
        this.palavratentativa = "";
        for (int i = 0; i < 5; i++) {
            this.palavratentativa += GameWindow.getLetterBoxes()[i].getText();
        }
    }
    public String getpalavratentativa(){
        return this.palavratentativa;
    }
    public void setPalavra(String palavra) {
        this.palavra = palavra;
    }
    public String getPalavra() {
        return palavra;
    }
    public char[] getResultado(){
        return coresresultado;
    }
}