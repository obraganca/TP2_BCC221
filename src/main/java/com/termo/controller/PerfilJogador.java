package com.termo.controller;

import com.termo.gui.components.RoundedBorder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.Arrays;
import com.termo.gui.StatsOverlay;

public class PerfilJogador implements Serializable {
    private Usuario usuario;
    private int jogos;
    private int vitorias;
    private int sequenciaVitorias;
    private int melhorSequencia;
    private int[] distribuicaoTentativas;

    // Componentes UI (transient para não serializar)


    public PerfilJogador(Usuario usuario) {
        this.usuario = usuario;
        this.jogos = 0;
        this.vitorias = 0;
        this.sequenciaVitorias = 0;
        this.melhorSequencia = 0;
        this.distribuicaoTentativas = new int[7]; // 0-5: vitórias, 6: derrotas
    }

    // Métodos de backend
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

    public void registrarDerrota(int tentativas) {
        jogos++;
        sequenciaVitorias = 0;
        distribuicaoTentativas[6]++; // índice 6 para derrotas
        salvarDados();
    }

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

    public void setUsuarioAfterDeserialization(Usuario usuario) {
        if (this.usuario == null) {
            this.usuario = usuario;
        }
    }

    // Métodos getters
    public Usuario getUsuario() {
        return usuario;
    }

    public int getJogos() {
        return jogos;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getSequenciaVitorias() {
        return sequenciaVitorias;
    }

    public int getMelhorSequencia() {
        return melhorSequencia;
    }

    public int[] getDistribuicaoTentativas() {
        return Arrays.copyOf(distribuicaoTentativas, distribuicaoTentativas.length);
    }

    public int getTentativasPorNumero(int numeroTentativa) {
        if (numeroTentativa >= 1 && numeroTentativa <= 6) {
            return distribuicaoTentativas[numeroTentativa - 1];
        } else if (numeroTentativa == 7) {
            return distribuicaoTentativas[6]; // derrotas
        }
        return 0;
    }

    public double getPercentualVitorias() {
        return (jogos == 0) ? 0 : (vitorias * 100.0) / jogos;
    }

    // Métodos setters
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setJogos(int jogos) {
        if (jogos >= 0) {
            this.jogos = jogos;
        }
    }

    public void setVitorias(int vitorias) {
        if (vitorias >= 0 && vitorias <= jogos) {
            this.vitorias = vitorias;
        }
    }

    public void setSequenciaVitorias(int sequenciaVitorias) {
        if (sequenciaVitorias >= 0) {
            this.sequenciaVitorias = sequenciaVitorias;
        }
    }

    public void setMelhorSequencia(int melhorSequencia) {
        if (melhorSequencia >= 0) {
            this.melhorSequencia = melhorSequencia;
        }
    }

    public void setDistribuicaoTentativas(int[] distribuicaoTentativas) {
        if (distribuicaoTentativas != null && distribuicaoTentativas.length == 7) {
            this.distribuicaoTentativas = Arrays.copyOf(distribuicaoTentativas, distribuicaoTentativas.length);
        }
    }

    public void setTentativasPorNumero(int numeroTentativa, int valor) {
        if (numeroTentativa >= 1 && numeroTentativa <= 6 && valor >= 0) {
            this.distribuicaoTentativas[numeroTentativa - 1] = valor;
        } else if (numeroTentativa == 7 && valor >= 0) {
            this.distribuicaoTentativas[6] = valor;
        }
    }
}