package com.termo.controller;

import java.io.*;
import java.util.Arrays;

public class Usuario implements Serializable {
    private String nome;
    private String senha;
    private PerfilJogador perfil;

    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
        // Garante que o perfil seja criado
        this.perfil = new PerfilJogador(this);
    }

    // Método para desserialização
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();

        // ⚠️ CRÍTICO: Se perfil for null após desserialização, cria um novo
        if (perfil == null) {
            perfil = new PerfilJogador(this);
            System.out.println("Perfil criado durante desserialização para: " + nome);
        } else {
            // Garante que o perfil tem a referência correta
            perfil.setUsuarioAfterDeserialization(this);
        }
    }

    public PerfilJogador getPerfil() {
        // ⚠️ SEGUNDA CAMADA DE PROTEÇÃO
        if (perfil == null) {
            perfil = new PerfilJogador(this);
            System.out.println("Perfil criado no getPerfil() para: " + nome);
        }
        return perfil;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }

    @Override
    public String toString() {
        return "Usuario{" + nome + ", perfil=" + (perfil != null ? "presente" : "null") + "}";
    }
}