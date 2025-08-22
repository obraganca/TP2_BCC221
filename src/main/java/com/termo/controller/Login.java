package com.termo.controller;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável pelo controle de login e cadastro de usuários.
 * Gerencia persistência em arquivo (usuarios.dat) e mantém usuários em memória.
 */
public class Login {
    private static Map<String, Usuario> usuarios = new HashMap<>(); // Banco de usuários em memória
    private static final String FILE_PATH = "usuarios.dat"; // Caminho do arquivo de persistência
    private Usuario usuarioLogado; // Usuário autenticado na sessão atual

    // Bloco estático: inicializa a lista de usuários a partir do arquivo.
    static {
        usuarios = carregarUsuarios();
    }

    private static String normalizarNome(String nome) {
        return nome != null ? nome.trim().toLowerCase() : null;
    }

    /**
     * loginOuCadastrar
     * Realiza login de um usuário ou cadastra caso ainda não exista.
     *
     * @param nome Nome do usuário
     * @param senha Senha do usuário
     * @return true se login/cadastro realizado com sucesso; false se senha incorreta
     */
    public boolean loginOuCadastrar(String nome, String senha) {
        if (usuarios.containsKey(nome)) {
            Usuario usuario = usuarios.get(nome);
            if (usuario.getSenha().equals(senha)) {
                this.usuarioLogado = usuario; // Autentica usuário
                return true;
            }
            return false; // Senha incorreta
        } else {
            // Cria novo usuário
            Usuario novoUsuario = new Usuario(nome, senha);
            usuarios.put(nome, novoUsuario);
            this.usuarioLogado = novoUsuario;
            salvarUsuarios(); // persiste no arquivo
            return true;
        }
    }

    /** @return Usuário pelo nome ou null se não existir */
    public static Usuario getUsuario(String nome) {
        return usuarios.get(nome);
    }

    /**
     * carregarUsuarios
     * Lê os usuários salvos em arquivo e carrega para memória.
     *
     * @return Mapa de usuários carregado
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Usuario> carregarUsuarios() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (Map<String, Usuario>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar usuários, iniciando com base vazia: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * salvarUsuarios
     * Persiste os usuários no arquivo (usuarios.dat).
     */
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

    /** Exibe no console os usuários carregados em memória (para debug) */
    public static void debugUsuarios() {
        System.out.println("=== USUÁRIOS NA MEMÓRIA ===");
        for (String key : usuarios.keySet()) {
            Usuario u = usuarios.get(key);
            System.out.println("Usuário: " + key +
                    ", Jogos: " + u.getPerfil().getJogos() +
                    ", Vitórias: " + u.getPerfil().getVitorias());
        }
    }

    /** Verifica existência e permissões do arquivo de persistência */
    public static void verificarArquivo() {
        File arquivo = new File(FILE_PATH);
        System.out.println("=== VERIFICAÇÃO DO ARQUIVO ===");
        System.out.println("Arquivo existe: " + arquivo.exists());
        System.out.println("Caminho absoluto: " + arquivo.getAbsolutePath());
        System.out.println("Pode escrever: " + arquivo.canWrite());
    }
}
