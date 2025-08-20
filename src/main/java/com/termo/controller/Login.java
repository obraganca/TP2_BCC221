package com.termo.controller;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Login {
    private static Map<String, Usuario> usuarios = new HashMap<>();
    private static final String FILE_PATH = "usuarios.dat";
    private Usuario usuarioLogado;

    // Bloco estático para inicialização
    static {
        usuarios = carregarUsuarios();
    }
    private static String normalizarNome(String nome) {
        return nome != null ? nome.trim().toLowerCase() : null;
    }

    public boolean loginOuCadastrar(String nome, String senha) {
        if (usuarios.containsKey(nome)) {
            Usuario usuario = usuarios.get(nome);
            if (usuario.getSenha().equals(senha)) {
                this.usuarioLogado = usuario; // ✅ Armazena o usuário logado
                return true;
            }
            return false;
        } else {
            Usuario novoUsuario = new Usuario(nome, senha);
            usuarios.put(nome, novoUsuario);
            this.usuarioLogado = novoUsuario; // ✅ Armazena o usuário logado
            salvarUsuarios();
            return true;
        }
    }


    public static Usuario getUsuario(String nome) {
        return usuarios.get(nome);
    }

    // === Persistência ===
    @SuppressWarnings("unchecked")
    public static Map<String, Usuario> carregarUsuarios() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (Map<String, Usuario>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar usuários, iniciando com base vazia: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void salvarUsuarios() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            System.out.println("=== SALVANDO USUÁRIOS ===");
            for (String key : usuarios.keySet()) {
                Usuario u = usuarios.get(key);
                System.out.println("Salvando: " + key +
                        ", Jogos: " + u.getPerfil().getJogos() +
                        ", Vitórias: " + u.getPerfil().getVitorias());
            }
            oos.writeObject(usuarios);
            System.out.println("✅ Usuários salvos com sucesso!");
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para debug
    public static void debugUsuarios() {
        System.out.println("=== USUÁRIOS NA MEMÓRIA ===");
        for (String key : usuarios.keySet()) {
            Usuario u = usuarios.get(key);
            System.out.println("Usuário: " + key +
                    ", Jogos: " + u.getPerfil().getJogos() +
                    ", Vitórias: " + u.getPerfil().getVitorias());
        }
    }

    // Método para verificar arquivo
    public static void verificarArquivo() {
        File arquivo = new File(FILE_PATH);
        System.out.println("=== VERIFICAÇÃO DO ARQUIVO ===");
        System.out.println("Arquivo existe: " + arquivo.exists());
        System.out.println("Caminho absoluto: " + arquivo.getAbsolutePath());
        System.out.println("Pode escrever: " + arquivo.canWrite());
    }
}