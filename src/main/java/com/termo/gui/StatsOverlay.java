package com.termo.gui;

import com.termo.gui.components.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Overlay de estatísticas responsivo (independente de GameWindow).
 */
public class StatsOverlay {

    private final JFrame owner;
    private final JLayeredPane layered;
    private final Stats stats = new Stats();

    // UI
    private JPanel overlayPanel;
    private JPanel statsCard;
    private JLabel totalGamesLabel;
    private JLabel winPercentLabel;
    private JLabel streakLabel;
    private JLabel bestStreakLabel;
    private JPanel distributionContainer;
    private boolean initialized = false;

    public StatsOverlay(JFrame owner) {
        this.owner = owner;
        this.layered = owner.getLayeredPane();
    }

    private void initIfNeeded() {
        if (initialized) return;
        initialized = true;

        overlayPanel = new JPanel(null);
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 150));
        overlayPanel.setBounds(0, 0, owner.getWidth(), owner.getHeight());
        overlayPanel.setVisible(false);
        overlayPanel.setFocusable(true);

        // card central responsivo
        statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBackground(Color.decode("#2f292a"));
        statsCard.setBorder(new RoundedBorder(12, "#2f292a", 6));
        statsCard.setOpaque(true);

        updateCardSize();

        // topo com título e botão fechar
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel title = new JLabel("progresso", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        // fonte responsiva
        int titleSize = Math.max(18, Math.min(32, owner.getWidth() / 30));
        title.setFont(new Font("Arial", Font.BOLD, titleSize));
        topRow.add(title, BorderLayout.CENTER);

        JButton closeBtn = new JButton("X");
        closeBtn.setFocusable(false);
        int btnSize = Math.max(24, Math.min(38, owner.getWidth() / 35));
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

        // métricas responsivas
        createMetricsPanel();

        statsCard.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel distTitle = new JLabel("distribuição de tentativas", JLabel.CENTER);
        distTitle.setForeground(Color.WHITE);
        int distTitleSize = Math.max(14, Math.min(20, owner.getWidth() / 40));
        distTitle.setFont(new Font("Arial", Font.BOLD, distTitleSize));
        distTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(distTitle);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        distributionContainer = new JPanel();
        distributionContainer.setOpaque(false);
        distributionContainer.setLayout(new BoxLayout(distributionContainer, BoxLayout.Y_AXIS));

        // padding responsivo
        int padding = Math.max(20, Math.min(50, owner.getWidth() / 25));
        distributionContainer.setBorder(BorderFactory.createEmptyBorder(8, padding, 8, padding));
        statsCard.add(distributionContainer);

        statsCard.add(Box.createVerticalGlue());

        // footer responsivo
        createFooterPanel();

        overlayPanel.add(statsCard);
        layered.add(overlayPanel, JLayeredPane.MODAL_LAYER);

        // eventos
        setupEventListeners();

        // inicializa distribuição
        rebuildDistribution();
    }

    private void createMetricsPanel() {
        // Decide o layout baseado no tamanho da tela
        boolean isSmallScreen = owner.getWidth() < 800 || owner.getHeight() < 600;

        JPanel metrics;
        if (isSmallScreen) {
            // Em telas pequenas, usa 2x2 grid
            metrics = new JPanel(new GridLayout(2, 2, 8, 8));
        } else {
            // Em telas normais, usa 1x4 grid
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

        // padding responsivo
        int vPadding = Math.max(5, Math.min(15, owner.getHeight() / 50));
        int hPadding = Math.max(15, Math.min(30, owner.getWidth() / 40));
        metricsWrapper.setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
        metricsWrapper.add(metrics, BorderLayout.CENTER);
        statsCard.add(metricsWrapper);
    }

    private void createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        // padding responsivo
        int padding = Math.max(10, Math.min(20, owner.getWidth() / 60));
        footer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        JLabel nextLabel = new JLabel("<html>próxima palavra em<br><span style='font-size:22pt'>15:02:51</span></html>");
        nextLabel.setForeground(Color.WHITE);
        footer.add(nextLabel, BorderLayout.WEST);

        JButton share = new JButton("compartilhe");
        share.setFocusable(false);

        // tamanho do botão responsivo
        int btnWidth = Math.max(150, Math.min(200, owner.getWidth() / 6));
        int btnHeight = Math.max(40, Math.min(60, owner.getHeight() / 15));
        int fontSize = Math.max(12, Math.min(18, owner.getWidth() / 70));

        share.setFont(new Font("Arial", Font.BOLD, fontSize));
        share.setPreferredSize(new Dimension(btnWidth, btnHeight));
        share.setBackground(Color.decode("#049CFF"));
        share.setForeground(Color.WHITE);
        share.setBorder(new RoundedBorder(12, "#049CFF", 4));
        footer.add(share, BorderLayout.EAST);

        statsCard.add(footer);
    }

    private void setupEventListeners() {
        // clique fora fecha
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

        // bloqueia clique no card
        statsCard.addMouseListener(new MouseAdapter() { /* consume */ });

        // ESC para fechar
        overlayPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        // reposiciona e redimensiona no resize
        owner.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlayPanel.setBounds(0, 0, owner.getWidth(), owner.getHeight());
                updateCardSize();
                updateResponsiveElements();
            }
        });
    }

    private void updateCardSize() {
        // Calcula tamanho do card baseado na tela
        int screenWidth = owner.getWidth();
        int screenHeight = owner.getHeight();

        // Percentuais responsivos
        double widthRatio = screenWidth < 600 ? 0.95 : (screenWidth < 1000 ? 0.85 : 0.65);
        double heightRatio = screenHeight < 500 ? 0.95 : (screenHeight < 800 ? 0.85 : 0.75);

        int cardW = (int) (screenWidth * widthRatio);
        int cardH = (int) (screenHeight * heightRatio);

        // Limites mínimos e máximos
        cardW = Math.max(320, Math.min(780, cardW));
        cardH = Math.max(400, Math.min(720, cardH));

        statsCard.setBounds((screenWidth - cardW) / 2, (screenHeight - cardH) / 2, cardW, cardH);
        statsCard.setPreferredSize(new Dimension(cardW, cardH));
        statsCard.setMaximumSize(new Dimension(cardW, cardH));
    }

    private void updateResponsiveElements() {
        if (!initialized) return;

        // Atualiza fontes e tamanhos dos componentes baseado no novo tamanho
        SwingUtilities.invokeLater(() -> {
            // Reconstrói métricas se necessário
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
                    // Ajusta tamanho da fonte baseado na tela
                    int newSize = Math.max(12, Math.min(28, owner.getWidth() / 40));
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

    // utilitários UI
    private JLabel makeMetricPanel(String value) {
        JLabel label = new JLabel(value, JLabel.CENTER);
        label.setForeground(Color.WHITE);

        // fonte responsiva para métricas
        int fontSize = Math.max(16, Math.min(28, owner.getWidth() / 40));
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

        // texto da legenda responsivo
        int legendSize = Math.max(10, Math.min(12, owner.getWidth() / 80));
        JLabel legend = new JLabel("<html><div style='text-align:center; font-size:" + legendSize + "px; color:#dcd9d9'>" + smallText + "</div></html>", JLabel.CENTER);
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(legend);
        return p;
    }

    private void rebuildDistribution() {
        if (distributionContainer == null) return;
        distributionContainer.removeAll();
        int[] dist = stats.attemptsDistribution;
        int max = 1;
        for (int i = 0; i < dist.length; i++) if (dist[i] > max) max = dist[i];

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

        // tamanhos responsivos para distribuição
        int labelWidth = Math.max(20, Math.min(30, owner.getWidth() / 40));
        int rowHeight = Math.max(20, Math.min(24, owner.getHeight() / 30));
        left.setPreferredSize(new Dimension(labelWidth, rowHeight));
        row.add(left, BorderLayout.WEST);

        JPanel barBg = new JPanel();
        barBg.setBackground(Color.decode("#312a2c"));
        barBg.setLayout(new BorderLayout());

        // largura da barra responsiva
        int maxBarWidth = Math.max(200, Math.min(420, owner.getWidth() - 200));
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
        right.setPreferredSize(new Dimension(Math.max(30, Math.min(40, owner.getWidth() / 30)), rowHeight));
        right.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    // API pública
    public void recordGame(boolean win, int attempts) {
        stats.recordGame(win, attempts);
    }

    public void show(boolean won) {
        SwingUtilities.invokeLater(() -> {
            initIfNeeded();
            totalGamesLabel.setText(String.valueOf(stats.totalGames));
            int pct = (stats.totalGames == 0) ? 0 : (int) ((stats.wins * 100.0) / stats.totalGames);
            winPercentLabel.setText(pct + "%");
            streakLabel.setText(String.valueOf(stats.currentStreak));
            bestStreakLabel.setText(String.valueOf(stats.bestStreak));

            rebuildDistribution();

            overlayPanel.setVisible(true);
            overlayPanel.requestFocusInWindow();
        });
    }

    public void hide() {
        SwingUtilities.invokeLater(() -> {
            if (!initialized) return;
            overlayPanel.setVisible(false);
        });
    }

    // Modelo interno de estatísticas
    private static class Stats {
        int totalGames = 0;
        int wins = 0;
        int currentStreak = 0;
        int bestStreak = 0;
        int[] attemptsDistribution = new int[7];

        void recordGame(boolean win, int attempts) {
            totalGames++;
            if (win) {
                wins++;
                currentStreak++;
                if (currentStreak > bestStreak) bestStreak = currentStreak;
                if (attempts >= 1 && attempts <= 6) {
                    attemptsDistribution[attempts - 1]++;
                } else {
                    attemptsDistribution[0]++;
                }
            } else {
                currentStreak = 0;
                attemptsDistribution[6]++;
            }
        }
    }
}