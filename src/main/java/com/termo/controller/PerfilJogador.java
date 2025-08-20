package com.termo.controller;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;

public class PerfilJogador implements Serializable {
    private Usuario usuario;
    private int jogos;
    private int vitorias;
    private int sequenciaVitorias;
    private int melhorSequencia;
    private int[] distribuicaoTentativas;

    // Remova os componentes UI da serialização com 'transient'
    private transient JPanel overlayPanel;
    private transient JLabel totalGamesLabel;
    private transient JLabel winPercentLabel;
    private transient JLabel streakLabel;
    private transient JLabel bestStreakLabel;
    private transient JPanel distributionContainer;
    private transient boolean uiInitialized = false;

    public PerfilJogador(Usuario usuario) {
        this.usuario = usuario;
        this.jogos = 0;
        this.vitorias = 0;
        this.sequenciaVitorias = 0;
        this.melhorSequencia = 0;
        this.distribuicaoTentativas = new int[6];
    }

    // ... métodos registrarVitoria, registrarDerrota, getters ...
    public void registrarVitoria(int tentativas) {
        jogos++;
        vitorias++;
        sequenciaVitorias++;
        if (sequenciaVitorias > melhorSequencia) {
            melhorSequencia = sequenciaVitorias;
        }
        if (tentativas >= 1 && tentativas <= 6) {
            distribuicaoTentativas[tentativas - 1]++;
        }
        salvarDados();
    }

    public void registrarDerrota(int tentativas) {
        jogos++;
        sequenciaVitorias = 0;
        if (tentativas >= 1 && tentativas <= 6) {
            distribuicaoTentativas[tentativas - 1]++;
        }
        salvarDados();
    }
    private void salvarDados() {
        if (usuario != null) {
            try {
                // ⚠️ ACESSA O LOGIN DIRETAMENTE PARA SALVAR
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
    public void show(boolean won, JFrame parentFrame) {
        SwingUtilities.invokeLater(() -> {
            initUI(parentFrame); // Sempre recria a UI
            System.out.println("=== DADOS DO PERFIL ===");
            System.out.println("Jogos: " + jogos);
            System.out.println("Vitórias: " + vitorias);
            System.out.println("Sequência: " + sequenciaVitorias);
            System.out.println("Melhor Seq: " + melhorSequencia);
            System.out.println("Distribuição: " + Arrays.toString(distribuicaoTentativas));

            totalGamesLabel.setText(String.valueOf(jogos));
            int pct = (jogos == 0) ? 0 : (int) ((vitorias * 100.0) / jogos);
            winPercentLabel.setText(pct + "%");
            streakLabel.setText(String.valueOf(sequenciaVitorias));
            bestStreakLabel.setText(String.valueOf(melhorSequencia));

            rebuildDistribution();

            overlayPanel.setVisible(true);
            overlayPanel.requestFocusInWindow();
        });
    }

    private void initUI(JFrame parentFrame) {
        if (uiInitialized && overlayPanel != null) return;

        // Remove overlay anterior se existir
        if (overlayPanel != null) {
            parentFrame.getLayeredPane().remove(overlayPanel);
        }

        // Cria o overlay panel
        overlayPanel = new JPanel(new GridBagLayout());
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 150));
        overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // Cria o card de estatísticas
        JPanel statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsCard.setBackground(Color.WHITE);
        statsCard.setPreferredSize(new Dimension(400, 500));

        // Título
        JLabel titleLabel = new JLabel("ESTATÍSTICAS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Painel de estatísticas principais
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        statsPanel.setBackground(Color.WHITE);

        // Criar os painéis de estatística (isso inicializa os JLabels)
        statsPanel.add(createStatPanel("Jogos", "0"));
        statsPanel.add(createStatPanel("Vitórias", "0%"));
        statsPanel.add(createStatPanel("Sequência", "0"));
        statsPanel.add(createStatPanel("Melhor Seq.", "0"));

        // Distribuição de tentativas
        JLabel distTitle = new JLabel("DISTRIBUIÇÃO DE TENTATIVAS");
        distTitle.setFont(new Font("Arial", Font.BOLD, 14));
        distTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        distTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        distributionContainer = new JPanel();
        distributionContainer.setLayout(new BoxLayout(distributionContainer, BoxLayout.Y_AXIS));
        distributionContainer.setBackground(Color.WHITE);

        // Botão de fechar
        JButton closeButton = new JButton("Fechar");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.addActionListener(e -> overlayPanel.setVisible(false));

        // Montar o card
        statsCard.add(titleLabel);
        statsCard.add(statsPanel);
        statsCard.add(distTitle);
        statsCard.add(Box.createRigidArea(new Dimension(0, 10)));
        statsCard.add(distributionContainer);
        statsCard.add(Box.createVerticalGlue());
        statsCard.add(closeButton);

        // Adicionar ao overlay
        overlayPanel.add(statsCard);
        parentFrame.getLayeredPane().add(overlayPanel, JLayeredPane.POPUP_LAYER);
        overlayPanel.setVisible(false);

        uiInitialized = true;
    }

    private JPanel createStatPanel(String title, String initialValue) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel valuePanel = new JPanel();
        valuePanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valuePanel.add(valueLabel);

        panel.add(titleLabel);
        panel.add(valuePanel);

        // Guardar referências para os labels
        switch (title) {
            case "Jogos": totalGamesLabel = valueLabel; break;
            case "Vitórias": winPercentLabel = valueLabel; break;
            case "Sequência": streakLabel = valueLabel; break;
            case "Melhor Seq.": bestStreakLabel = valueLabel; break;
        }

        return panel;
    }
    private void rebuildDistribution() {
        if (distributionContainer == null) return;

        distributionContainer.removeAll();

        int maxTentativas = Arrays.stream(distribuicaoTentativas).max().orElse(1);

        for (int i = 0; i < distribuicaoTentativas.length; i++) {
            JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setMaximumSize(new Dimension(350, 30));
            rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JLabel tentativaLabel = new JLabel(String.valueOf(i + 1));
            tentativaLabel.setFont(new Font("Arial", Font.BOLD, 14));
            tentativaLabel.setPreferredSize(new Dimension(30, 20));

            JProgressBar bar = new JProgressBar(0, maxTentativas);
            bar.setValue(distribuicaoTentativas[i]);
            bar.setStringPainted(true);
            bar.setString(distribuicaoTentativas[i] > 0 ? String.valueOf(distribuicaoTentativas[i]) : "");
            bar.setForeground(new Color(106, 170, 100));

            JLabel countLabel = new JLabel(String.valueOf(distribuicaoTentativas[i]));
            countLabel.setFont(new Font("Arial", Font.BOLD, 14));
            countLabel.setPreferredSize(new Dimension(30, 20));

            rowPanel.add(tentativaLabel, BorderLayout.WEST);
            rowPanel.add(bar, BorderLayout.CENTER);
            rowPanel.add(countLabel, BorderLayout.EAST);

            distributionContainer.add(rowPanel);
        }

        distributionContainer.revalidate();
        distributionContainer.repaint();
    }


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
        if (numeroTentativa >= 1 && numeroTentativa <= 5) {
            return distribuicaoTentativas[numeroTentativa - 1];
        }
        return 0;
    }

    public double getPercentualVitorias() {
        return (jogos == 0) ? 0 : (vitorias * 100.0) / jogos;
    }

    // === SETTERS ===
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
        if (distribuicaoTentativas != null && distribuicaoTentativas.length == 6) {
            this.distribuicaoTentativas = Arrays.copyOf(distribuicaoTentativas, distribuicaoTentativas.length);
        }
    }

    public void setTentativasPorNumero(int numeroTentativa, int valor) {
        if (numeroTentativa >= 1 && numeroTentativa <= 6 && valor >= 0) {
            this.distribuicaoTentativas[numeroTentativa - 1] = valor;
        }
    }
}