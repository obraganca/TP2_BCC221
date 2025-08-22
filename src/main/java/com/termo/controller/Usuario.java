package com.termo.controller;

import java.io.*;
import java.util.Arrays;

public class Usuario implements Serializable {
    private String nome;              // Nome do usuário
    private String senha;             // Senha do usuário
    private PerfilJogador perfil;     // Perfil associado ao usuário

    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
        // Garante que o perfil seja criado no momento da construção
        this.perfil = new PerfilJogador(this);
    }

    // Método especial usado durante a desserialização de objetos
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Lê os campos padrão serializados

        //Se o perfil não existir após a desserialização, cria um novo
        if (perfil == null) {
            perfil = new PerfilJogador(this);
            System.out.println("Perfil criado durante desserialização para: " + nome);
        } else {
            // Caso já exista, ajusta a referência para o objeto Usuario atual
            perfil.setUsuarioAfterDeserialization(this);
        }
    }

    public PerfilJogador getPerfil() {
        // caso alguém acesse antes de existir perfil, cria na hora
        if (perfil == null) {
            perfil = new PerfilJogador(this);
            System.out.println("Perfil criado no getPerfil() para: " + nome);
        }
        return perfil;
    }

    public String getNome() {
        return nome; // Retorna o nome do usuário
    }

    public String getSenha() {
        return senha; // Retorna a senha do usuário
    }

    @Override
    public String toString() {
        // Representação textual do objeto, indicando se o perfil existe
        return "Usuario{" + nome + ", perfil=" + (perfil != null ? "presente" : "null") + "}";
    }
}
