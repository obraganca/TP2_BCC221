package com.termo.controller;

import com.termo.gui.components.RoundedBorder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.Arrays;

public class PerfilJogador implements Serializable {
    private Usuario usuario;
    private int jogos;
    private int vitorias;
    private int sequenciaVitorias;
    private int melhorSequencia;
    private int[] distribuicaoTentativas;

    // Componentes UI (transient para não serializar)
    private transient JFrame parentFrame;
    private transient JLayeredPane layered;
    private transient JPanel overlayPanel;
    private transient JPanel statsCard;
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

    // Interface UI
    public void show(boolean won, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        SwingUtilities.invokeLater(() -> {
            initUI();
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

    public void hide() {
        SwingUtilities.invokeLater(() -> {
            if (!uiInitialized) return;
            overlayPanel.setVisible(false);
        });
    }

    private void initUI() {
        if (uiInitialized) return;
        uiInitialized = true;

        layered = parentFrame.getLayeredPane();

        overlayPanel = new JPanel(null);
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 150));
        overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        overlayPanel.setVisible(false);
        overlayPanel.setFocusable(true);

        // Card central responsivo
        statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBackground(Color.decode("#2f292a"));
        statsCard.setBorder(new RoundedBorder(12, "#2f292a", 6));
        statsCard.setOpaque(true);

        updateCardSize();

        // Topo com título e botão fechar
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel title = new JLabel("progresso", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        int titleSize = Math.max(18, Math.min(32, parentFrame.getWidth() / 30));
        title.setFont(new Font("Arial", Font.BOLD, titleSize));
        topRow.add(title, BorderLayout.CENTER);

        JButton closeBtn = new JButton("X");
        closeBtn.setFocusable(false);
        int btnSize = Math.max(24, Math.min(38, parentFrame.getWidth() / 35));
        closeBtn.setPreferredSize(new Dimension(btnSize, btnSize));
        closeBtn.setBorder(new RoundedBorder(6, "#4c4347", 2));
        closeBtn.setBackground(Color.decode("#4c4347"));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> hide());
        topRow.add(closeBtn, BorderLayout.EAST);

        statsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        topRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(topRow);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // Métricas responsivas
        createMetricsPanel();

        statsCard.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel distTitle = new JLabel("distribuição de tentativas", JLabel.CENTER);
        distTitle.setForeground(Color.WHITE);
        int distTitleSize = Math.max(14, Math.min(20, parentFrame.getWidth() / 40));
        distTitle.setFont(new Font("Arial", Font.BOLD, distTitleSize));
        distTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(distTitle);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        distributionContainer = new JPanel();
        distributionContainer.setOpaque(false);
        distributionContainer.setLayout(new BoxLayout(distributionContainer, BoxLayout.Y_AXIS));

        int padding = Math.max(20, Math.min(50, parentFrame.getWidth() / 25));
        distributionContainer.setBorder(BorderFactory.createEmptyBorder(8, padding, 8, padding));
        statsCard.add(distributionContainer);

        statsCard.add(Box.createVerticalGlue());

        // Footer responsivo
        createFooterPanel();

        overlayPanel.add(statsCard);
        layered.add(overlayPanel, JLayeredPane.MODAL_LAYER);

        // Eventos
        setupEventListeners();

        // Inicializa distribuição
        rebuildDistribution();
    }

    private void createMetricsPanel() {
        boolean isSmallScreen = parentFrame.getWidth() < 800 || parentFrame.getHeight() < 600;

        JPanel metrics;
        if (isSmallScreen) {
            metrics = new JPanel(new GridLayout(2, 2, 8, 8));
        } else {
            metrics = new JPanel(new GridLayout(1, 4, 8, 8));
        }
        metrics.setOpaque(false);

        totalGamesLabel = makeMetricPanel("0");
        winPercentLabel = makeMetricPanel("0%");
        streakLabel = makeMetricPanel("0");
        bestStreakLabel = makeMetricPanel("0");

        metrics.add(wrapMetric(totalGamesLabel, "jogos"));
        metrics.add(wrapMetric(winPercentLabel, "de vitórias"));
        metrics.add(wrapMetric(streakLabel, "sequência\nde vitórias"));
        metrics.add(wrapMetric(bestStreakLabel, "melhor\nsequência"));

        JPanel metricsWrapper = new JPanel(new BorderLayout());
        metricsWrapper.setOpaque(false);

        int vPadding = Math.max(5, Math.min(15, parentFrame.getHeight() / 50));
        int hPadding = Math.max(15, Math.min(30, parentFrame.getWidth() / 40));
        metricsWrapper.setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
        metricsWrapper.add(metrics, BorderLayout.CENTER);
        statsCard.add(metricsWrapper);
    }

    private void createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        int padding = Math.max(10, Math.min(20, parentFrame.getWidth() / 60));
        footer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        JButton share = new JButton("compartilhe");
        share.setFocusable(false);

        int btnWidth = Math.max(150, Math.min(200, parentFrame.getWidth() / 6));
        int btnHeight = Math.max(40, Math.min(60, parentFrame.getHeight() / 15));
        int fontSize = Math.max(12, Math.min(18, parentFrame.getWidth() / 70));

        share.setFont(new Font("Arial", Font.BOLD, fontSize));
        share.setPreferredSize(new Dimension(btnWidth, btnHeight));
        share.setBackground(Color.decode("#049CFF"));
        share.setForeground(Color.WHITE);
        share.setBorder(new RoundedBorder(12, "#049CFF", 4));
        footer.add(share, BorderLayout.EAST);

        statsCard.add(footer);
    }

    private void setupEventListeners() {
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                Rectangle bounds = statsCard.getBounds();
                if (!bounds.contains(p)) {
                    hide();
                }
            }
        });

        statsCard.addMouseListener(new MouseAdapter() {});

        overlayPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        parentFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
                updateCardSize();
                updateResponsiveElements();
            }
        });
    }

    private void updateCardSize() {
        int screenWidth = parentFrame.getWidth();
        int screenHeight = parentFrame.getHeight();

        double widthRatio = screenWidth < 600 ? 0.95 : (screenWidth < 1000 ? 0.85 : 0.65);
        double heightRatio = screenHeight < 500 ? 0.95 : (screenHeight < 800 ? 0.85 : 0.75);

        int cardW = (int) (screenWidth * widthRatio);
        int cardH = (int) (screenHeight * heightRatio);

        cardW = Math.max(320, Math.min(780, cardW));
        cardH = Math.max(400, Math.min(720, cardH));

        statsCard.setBounds((screenWidth - cardW) / 2, (screenHeight - cardH) / 2, cardW, cardH);
        statsCard.setPreferredSize(new Dimension(cardW, cardH));
        statsCard.setMaximumSize(new Dimension(cardW, cardH));
    }

    private void updateResponsiveElements() {
        if (!uiInitialized) return;

        SwingUtilities.invokeLater(() -> {
            Component[] components = statsCard.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    updatePanelFonts((JPanel) comp);
                }
            }

            rebuildDistribution();
            statsCard.revalidate();
            statsCard.repaint();
        });
    }

    private void updatePanelFonts(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Font currentFont = label.getFont();
                if (currentFont != null) {
                    int newSize = Math.max(12, Math.min(28, parentFrame.getWidth() / 40));
                    if (currentFont.isBold()) {
                        label.setFont(new Font(currentFont.getName(), Font.BOLD, newSize));
                    } else {
                        label.setFont(new Font(currentFont.getName(), Font.PLAIN, newSize));
                    }
                }
            } else if (comp instanceof JPanel) {
                updatePanelFonts((JPanel) comp);
            }
        }
    }

    private JLabel makeMetricPanel(String value) {
        JLabel label = new JLabel(value, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        int fontSize = Math.max(16, Math.min(28, parentFrame.getWidth() / 40));
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel wrapMetric(JLabel bigLabel, String smallText) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        bigLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(bigLabel);

        int legendSize = Math.max(10, Math.min(12, parentFrame.getWidth() / 80));
        JLabel legend = new JLabel("<html><div style='text-align:center; font-size:" + legendSize + "px; color:#dcd9d9'>" + smallText + "</div></html>", JLabel.CENTER);
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(legend);
        return p;
    }

    private void rebuildDistribution() {
        if (distributionContainer == null) return;
        distributionContainer.removeAll();

        int[] dist = distribuicaoTentativas;
        int max = 1;
        for (int value : dist) {
            if (value > max) max = value;
        }

        for (int i = 0; i < 6; i++) {
            int count = dist[i];
            distributionContainer.add(makeDistributionRow(String.valueOf(i + 1), count, max));
            distributionContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        distributionContainer.add(makeDistributionRow("\u2620", dist[6], Math.max(1, max)));
        distributionContainer.revalidate();
        distributionContainer.repaint();
    }

    private JPanel makeDistributionRow(String label, int count, int maxCount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel left = new JLabel(label);
        left.setForeground(Color.WHITE);

        int labelWidth = Math.max(20, Math.min(30, parentFrame.getWidth() / 40));
        int rowHeight = Math.max(20, Math.min(24, parentFrame.getHeight() / 30));
        left.setPreferredSize(new Dimension(labelWidth, rowHeight));
        row.add(left, BorderLayout.WEST);

        JPanel barBg = new JPanel();
        barBg.setBackground(Color.decode("#312a2c"));
        barBg.setLayout(new BorderLayout());

        int maxBarWidth = Math.max(200, Math.min(420, parentFrame.getWidth() - 200));
        barBg.setPreferredSize(new Dimension(maxBarWidth, rowHeight));
        barBg.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        int w = (maxCount == 0) ? 0 : (int) ((maxBarWidth * (double) count) / maxCount);
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(w, rowHeight - 6));
        bar.setBackground(Color.decode("#049CFF"));
        barBg.add(bar, BorderLayout.WEST);

        row.add(barBg, BorderLayout.CENTER);

        JLabel right = new JLabel(String.valueOf(count));
        right.setForeground(Color.WHITE);
        right.setPreferredSize(new Dimension(Math.max(30, Math.min(40, parentFrame.getWidth() / 30)), rowHeight));
        right.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }
}