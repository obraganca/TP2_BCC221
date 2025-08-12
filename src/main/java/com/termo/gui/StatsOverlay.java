package com.termo.gui;

import com.termo.gui.components.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Overlay de estatísticas (independente de GameWindow).
 * Requer a classe RoundedBorder no mesmo projeto.
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

    // inicializa os componentes (chame quando owner já tiver tamanho adequado;
    // é seguro chamar mais de uma vez)
    private void initIfNeeded() {
        if (initialized) return;
        initialized = true;

        overlayPanel = new JPanel(null);
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 150));
        overlayPanel.setBounds(0, 0, owner.getWidth(), owner.getHeight());
        overlayPanel.setVisible(false);
        overlayPanel.setFocusable(true);

        // card central
        statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBackground(Color.decode("#2f292a"));
        statsCard.setBorder(new RoundedBorder(12, "#2f292a", 6));
        statsCard.setOpaque(true);

        int cardW = Math.min(780, owner.getWidth() - 200);
        int cardH = Math.min(720, owner.getHeight() - 200);
        statsCard.setBounds((owner.getWidth() - cardW) / 2, (owner.getHeight() - cardH) / 2, cardW, cardH);
        statsCard.setPreferredSize(new Dimension(cardW, cardH));
        statsCard.setMaximumSize(new Dimension(cardW, cardH));

        // topo com título e botão fechar
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel title = new JLabel("progresso", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        topRow.add(title, BorderLayout.CENTER);

        JButton closeBtn = new JButton("X");
        closeBtn.setFocusable(false);
        closeBtn.setPreferredSize(new Dimension(38, 28));
        closeBtn.setBorder(new RoundedBorder(6, "#4c4347", 2));
        closeBtn.setBackground(Color.decode("#4c4347"));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> hide());
        topRow.add(closeBtn, BorderLayout.EAST);

        statsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        topRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(topRow);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // métricas
        JPanel metrics = new JPanel(new GridLayout(1, 4, 8, 8));
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
        metricsWrapper.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        metricsWrapper.add(metrics, BorderLayout.CENTER);
        statsCard.add(metricsWrapper);

        statsCard.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel distTitle = new JLabel("distribuição de tentativas", JLabel.CENTER);
        distTitle.setForeground(Color.WHITE);
        distTitle.setFont(new Font("Arial", Font.BOLD, 20));
        distTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(distTitle);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        distributionContainer = new JPanel();
        distributionContainer.setOpaque(false);
        distributionContainer.setLayout(new BoxLayout(distributionContainer, BoxLayout.Y_AXIS));
        distributionContainer.setBorder(BorderFactory.createEmptyBorder(8, 50, 8, 50));
        statsCard.add(distributionContainer);

        statsCard.add(Box.createVerticalGlue());

        // footer com botão compartilhar (render simples)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel nextLabel = new JLabel("<html>próxima palavra em<br><span style='font-size:22pt'>15:02:51</span></html>");
        nextLabel.setForeground(Color.WHITE);
        footer.add(nextLabel, BorderLayout.WEST);

        JButton share = new JButton("compartilhe");
        share.setFocusable(false);
        share.setFont(new Font("Arial", Font.BOLD, 18));
        share.setPreferredSize(new Dimension(200, 60));
        share.setBackground(Color.decode("#049CFF"));
        share.setForeground(Color.WHITE);
        share.setBorder(new RoundedBorder(12, "#049CFF", 4));
        footer.add(share, BorderLayout.EAST);

        statsCard.add(footer);

        overlayPanel.add(statsCard);
        layered.add(overlayPanel, JLayeredPane.MODAL_LAYER);

        // clique fora fecha
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // se clicar fora do card
                Point p = e.getPoint();
                Rectangle bounds = statsCard.getBounds();
                if (!bounds.contains(p)) {
                    hide();
                }
            }
        });

        // bloqueia clique no card (para não propagar)
        statsCard.addMouseListener(new MouseAdapter() { /* consume */ });

        // ESC para fechar (quando overlay visível)
        overlayPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        // reposiciona no resize
        owner.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlayPanel.setBounds(0, 0, owner.getWidth(), owner.getHeight());
                int w = statsCard.getWidth();
                int h = statsCard.getHeight();
                statsCard.setBounds((owner.getWidth() - w) / 2, (owner.getHeight() - h) / 2, w, h);
            }
        });

        // inicializa distribuição
        rebuildDistribution();
    }

    // utilitários UI
    private JLabel makeMetricPanel(String value) {
        JLabel label = new JLabel(value, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel wrapMetric(JLabel bigLabel, String smallText) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        bigLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(bigLabel);
        JLabel legend = new JLabel("<html><div style='text-align:center; font-size:12px; color:#dcd9d9'>" + smallText + "</div></html>", JLabel.CENTER);
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
        left.setPreferredSize(new Dimension(30, 24));
        row.add(left, BorderLayout.WEST);

        JPanel barBg = new JPanel();
        barBg.setBackground(Color.decode("#312a2c"));
        barBg.setLayout(new BorderLayout());
        barBg.setPreferredSize(new Dimension(420, 24));
        barBg.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        int maxWidth = 420;
        int w = (maxCount == 0) ? 0 : (int) ((maxWidth * (double) count) / maxCount);
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(w, 18));
        bar.setBackground(Color.decode("#049CFF"));
        barBg.add(bar, BorderLayout.WEST);

        row.add(barBg, BorderLayout.CENTER);

        JLabel right = new JLabel(String.valueOf(count));
        right.setForeground(Color.WHITE);
        right.setPreferredSize(new Dimension(40, 24));
        right.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    // API pública

    /**
     * Registra um jogo nas estatísticas.
     *
     * @param win      se venceu
     * @param attempts número de tentativas (1..6) quando venceu; ignored quando perdeu
     */
    public void recordGame(boolean win, int attempts) {
        stats.recordGame(win, attempts);
    }

    public void show(boolean won) {
        // garante init e atualiza labels
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

    // Modelo interno de estatísticas (simples)
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
