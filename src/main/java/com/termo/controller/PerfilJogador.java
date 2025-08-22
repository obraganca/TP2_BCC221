package com.termo.controller;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Classe que representa o perfil estatístico de um jogador.
 * Armazena dados como número de jogos, vitórias, sequências e distribuição de tentativas.
 * Também é responsável por atualizar e persistir esses dados.
 */
public class PerfilJogador implements Serializable {
    private Usuario usuario; // Usuário dono do perfil
    private int jogos; // Total de jogos
    private int vitorias; // Total de vitórias
    private int sequenciaVitorias; // Sequência atual de vitórias
    private int melhorSequencia; // Melhor sequência registrada
    private int[] distribuicaoTentativas; // [0-5] vitórias em 1–6 tentativas, [6] derrotas

    /**
     * Construtor do perfil.
     * Inicializa estatísticas zeradas.
     *
     * @param usuario Usuário associado ao perfil
     */
    public PerfilJogador(Usuario usuario) {
        this.usuario = usuario;
        this.jogos = 0;
        this.vitorias = 0;
        this.sequenciaVitorias = 0;
        this.melhorSequencia = 0;
        this.distribuicaoTentativas = new int[7]; // 0–5 vitórias, 6 derrotas
    }

    /**
     * registrarVitoria
     * Atualiza estatísticas do jogador após uma vitória.
     *
     * @param tentativas Número de tentativas usadas para vencer
     */
    public void registrarVitoria(int tentativas) {
        jogos++;
        vitorias++;
        sequenciaVitorias++;
        if (sequenciaVitorias > melhorSequencia) {
            melhorSequencia = sequenciaVitorias;
        }
        if (tentativas >= 1 && tentativas <= 6) {
            distribuicaoTentativas[tentativas - 1]++;
        } else {
            distribuicaoTentativas[0]++; // fallback
        }
        salvarDados();
    }

    /**
     * registrarDerrota
     * Atualiza estatísticas após uma derrota.
     *
     * @param tentativas Número de tentativas feitas antes da derrota
     */
    public void registrarDerrota(int tentativas) {
        jogos++;
        sequenciaVitorias = 0;
        distribuicaoTentativas[6]++; // índice 6 = derrotas
        salvarDados();
    }

    /**
     * salvarDados
     * Persiste os dados do perfil no arquivo de usuários.
     */
    private void salvarDados() {
        if (usuario != null) {
            try {
                Login.salvarUsuarios();
                System.out.println("Dados salvos após modificação!");
            } catch (Exception e) {
                System.err.println("Erro ao salvar dados: " + e.getMessage());
            }
        }
    }

    /**
     * setUsuarioAfterDeserialization
     * Reassocia o objeto Usuario após desserialização.
     *
     * @param usuario Usuário a ser associado
     */
    public void setUsuarioAfterDeserialization(Usuario usuario) {
        if (this.usuario == null) {
            this.usuario = usuario;
        }
    }

    // ===== Getters =====
    public Usuario getUsuario() { return usuario; }
    public int getJogos() { return jogos; }
    public int getVitorias() { return vitorias; }
    public int getSequenciaVitorias() { return sequenciaVitorias; }
    public int getMelhorSequencia() { return melhorSequencia; }

    /** @return Cópia da distribuição de tentativas (vitórias e derrotas) */
    public int[] getDistribuicaoTentativas() {
        return Arrays.copyOf(distribuicaoTentativas, distribuicaoTentativas.length);
    }

    /**
     * getTentativasPorNumero
     * Retorna quantas vezes o jogador venceu em determinada tentativa
     * ou quantas derrotas acumulou.
     *
     * @param numeroTentativa 1–6 para vitórias, 7 para derrotas
     * @return Quantidade de ocorrências
     */
    public int getTentativasPorNumero(int numeroTentativa) {
        if (numeroTentativa >= 1 && numeroTentativa <= 6) {
            return distribuicaoTentativas[numeroTentativa - 1];
        } else if (numeroTentativa == 7) {
            return distribuicaoTentativas[6]; // derrotas
        }
        return 0;
    }

    /** @return Percentual de vitórias em relação ao total de jogos */
    public double getPercentualVitorias() {
        return (jogos == 0) ? 0 : (vitorias * 100.0) / jogos;
    }

    // ===== Setters =====
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setJogos(int jogos) { if (jogos >= 0) this.jogos = jogos; }
    public void setVitorias(int vitorias) { if (vitorias >= 0 && vitorias <= jogos) this.vitorias = vitorias; }
    public void setSequenciaVitorias(int sequenciaVitorias) { if (sequenciaVitorias >= 0) this.sequenciaVitorias = sequenciaVitorias; }
    public void setMelhorSequencia(int melhorSequencia) { if (melhorSequencia >= 0) this.melhorSequencia = melhorSequencia; }

    /** Define a distribuição completa de tentativas (precisa ter 7 posições) */
    public void setDistribuicaoTentativas(int[] distribuicaoTentativas) {
        if (distribuicaoTentativas != null && distribuicaoTentativas.length == 7) {
            this.distribuicaoTentativas = Arrays.copyOf(distribuicaoTentativas, distribuicaoTentativas.length);
        }
    }

    /** Define o número de vitórias/derrotas em uma tentativa específica */
    public void setTentativasPorNumero(int numeroTentativa, int valor) {
        if (numeroTentativa >= 1 && numeroTentativa <= 6 && valor >= 0) {
            this.distribuicaoTentativas[numeroTentativa - 1] = valor;
        } else if (numeroTentativa == 7 && valor >= 0) {
            this.distribuicaoTentativas[6] = valor;
        }
    }
}
