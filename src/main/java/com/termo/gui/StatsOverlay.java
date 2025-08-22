package com.termo.gui;

import com.termo.gui.components.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.termo.controller.PerfilJogador;

/**
 * Representa um painel de sobreposição (overlay) que exibe as estatísticas do jogador.
 * Esta classe é projetada para ser responsiva e independente da janela principal do jogo.
 */
public class StatsOverlay {
    // Objeto que contém os dados das estatísticas do jogador.
    PerfilJogador perfilJogador;
    // Referências a componentes Swing para construir e gerenciar a interface do overlay.
    private transient JFrame parentFrame; // A janela principal sobre a qual o overlay é mostrado.
    private transient JLayeredPane layered; // Painel que permite sobrepor componentes.
    private transient JPanel overlayPanel; // O painel de fundo semi-transparente.
    private transient JPanel statsCard; // O "cartão" central que contém as estatísticas.
    private transient JLabel totalGamesLabel, winPercentLabel, streakLabel, bestStreakLabel; // Labels para os números.
    private transient JPanel distributionContainer; // Painel para as barras de distribuição de tentativas.
    private transient boolean uiInitialized = false; // Flag para garantir que a UI seja inicializada apenas uma vez.

    // Callback opcional a ser executado quando o overlay for fechado.
    private transient Runnable onClose;

    /**
     * Construtor do StatsOverlay.
     * @param perfilJogador O perfil do jogador contendo as estatísticas.
     */
    public StatsOverlay(PerfilJogador perfilJogador) {
        this.perfilJogador = perfilJogador;
    }

    /**
     * Mostra o overlay de estatísticas.
     * @param won Status de vitória do jogo (não utilizado nesta versão do método).
     * @param parentFrame A janela principal do jogo.
     */
    public void show(boolean won, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        // Garante que a criação da UI ocorra na Event Dispatch Thread (EDT) do Swing.
        SwingUtilities.invokeLater(() -> {
            initUI(); // Inicializa a UI se ainda não foi feito.
            // Atualiza os textos das labels com os dados mais recentes do perfil.
            totalGamesLabel.setText(String.valueOf(perfilJogador.getJogos()));
            int pct = (perfilJogador.getJogos() == 0) ? 0 : (int) ((perfilJogador.getVitorias() * 100.0) / perfilJogador.getJogos());
            winPercentLabel.setText(pct + "%");
            streakLabel.setText(String.valueOf(perfilJogador.getSequenciaVitorias()));
            bestStreakLabel.setText(String.valueOf(perfilJogador.getMelhorSequencia()));

            rebuildDistribution(); // Recria o gráfico de distribuição.

            overlayPanel.setVisible(true); // Torna o overlay visível.
            overlayPanel.requestFocusInWindow(); // Solicita foco para capturar eventos de teclado (como ESC).
        });
    }

    /**
     * Versão sobrecarregada do método show que aceita um callback a ser executado ao fechar.
     * Útil para encadear ações, como voltar para a tela de login.
     * @param won Status de vitória do jogo.
     * @param parentFrame A janela principal.
     * @param onClose Ação (Runnable) a ser executada quando o overlay for fechado.
     */
    public void show(boolean won, JFrame parentFrame, Runnable onClose) {
        this.onClose = onClose;
        // Chama a outra versão do método show para evitar duplicação de código.
        this.show(won, parentFrame);
    }


    /**
     * Esconde o painel de overlay.
     */
    public void hide() {
        SwingUtilities.invokeLater(() -> {
            if (!uiInitialized) return;
            overlayPanel.setVisible(false);
            // Se um callback 'onClose' foi definido, ele é executado aqui.
            if (onClose != null) {
                Runnable cb = onClose;
                onClose = null; // Garante que seja executado apenas uma vez.
                try {
                    cb.run(); // Executa a ação de callback.
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    /**
     * Inicializa todos os componentes da interface gráfica do overlay.
     * Este método é chamado apenas uma vez, graças à flag `uiInitialized`.
     */
    private void initUI() {
        if (uiInitialized) return;
        uiInitialized = true;

        layered = parentFrame.getLayeredPane();

        // Painel de fundo que cobre toda a tela.
        overlayPanel = new JPanel(null); // Layout nulo para posicionamento absoluto do 'statsCard'.
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 150)); // Cor preta com 150 de alfa (transparência).
        overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        overlayPanel.setVisible(false);
        overlayPanel.setFocusable(true);

        // O "cartão" central que agrupa todo o conteúdo.
        statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS)); // Layout vertical.
        statsCard.setBackground(Color.decode("#2f292a"));
        statsCard.setBorder(new RoundedBorder(12, "#2f292a", 6));
        statsCard.setOpaque(true);

        updateCardSize(); // Define o tamanho responsivo do cartão.

        // Linha superior com título e botão de fechar.
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel title = new JLabel("progresso", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        // Tamanho da fonte responsivo, calculado com base na largura da janela.
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

        statsCard.add(Box.createRigidArea(new Dimension(0, 8))); // Espaçamento.
        topRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(topRow);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12))); // Espaçamento.

        // Cria o painel de métricas (jogos, vitórias, etc.).
        createMetricsPanel();

        statsCard.add(Box.createRigidArea(new Dimension(0, 18))); // Espaçamento.

        // Título para a seção de distribuição.
        JLabel distTitle = new JLabel("distribuição de tentativas", JLabel.CENTER);
        distTitle.setForeground(Color.WHITE);
        int distTitleSize = Math.max(14, Math.min(20, parentFrame.getWidth() / 40));
        distTitle.setFont(new Font("Arial", Font.BOLD, distTitleSize));
        distTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsCard.add(distTitle);

        statsCard.add(Box.createRigidArea(new Dimension(0, 12))); // Espaçamento.

        // Container para as barras de distribuição.
        distributionContainer = new JPanel();
        distributionContainer.setOpaque(false);
        distributionContainer.setLayout(new BoxLayout(distributionContainer, BoxLayout.Y_AXIS));

        int padding = Math.max(20, Math.min(50, parentFrame.getWidth() / 25));
        distributionContainer.setBorder(BorderFactory.createEmptyBorder(8, padding, 8, padding));
        statsCard.add(distributionContainer);

        statsCard.add(Box.createVerticalGlue()); // Empurra o rodapé para baixo.

        // Cria o painel de rodapé com o botão de compartilhar.
        createFooterPanel();

        overlayPanel.add(statsCard);
        layered.add(overlayPanel, JLayeredPane.MODAL_LAYER); // Adiciona o overlay na camada modal para ficar por cima.

        // Configura os listeners de eventos (mouse, teclado, redimensionamento).
        setupEventListeners();

        // Inicializa o gráfico de distribuição.
        rebuildDistribution();
    }

    /**
     * Cria o painel que exibe as quatro principais métricas.
     * O layout muda de uma linha (1x4) para duas (2x2) em telas pequenas para melhor visualização.
     */
    private void createMetricsPanel() {
        boolean isSmallScreen = parentFrame.getWidth() < 800 || parentFrame.getHeight() < 600;

        JPanel metrics;
        if (isSmallScreen) {
            metrics = new JPanel(new GridLayout(2, 2, 8, 8));
        } else {
            metrics = new JPanel(new GridLayout(1, 4, 8, 8));
        }
        metrics.setOpaque(false);

        // Cria as labels que mostrarão os números das estatísticas.
        totalGamesLabel = makeMetricPanel("0");
        winPercentLabel = makeMetricPanel("0%");
        streakLabel = makeMetricPanel("0");
        bestStreakLabel = makeMetricPanel("0");

        // Agrupa cada label de número com sua legenda correspondente.
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

    /**
     * Cria o painel do rodapé, contendo o botão "compartilhe".
     */
    private void createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        int padding = Math.max(10, Math.min(20, parentFrame.getWidth() / 60));
        footer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        JButton share = new JButton("compartilhe");
        share.setFocusable(false);

        // Tamanho e fonte responsivos para o botão.
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

    /**
     * Configura os listeners de eventos para o overlay.
     */
    private void setupEventListeners() {
        // Listener para fechar o overlay ao clicar na área escura (fora do 'statsCard').
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

        // Consome cliques do mouse no 'statsCard' para evitar que eles fechem o overlay.
        statsCard.addMouseListener(new MouseAdapter() {});

        // Listener para fechar o overlay ao pressionar a tecla ESC.
        overlayPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        // Listener para redimensionar o overlay quando a janela principal muda de tamanho.
        parentFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlayPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
                updateCardSize();
                updateResponsiveElements();
            }
        });
    }

    /**
     * Calcula e atualiza o tamanho e a posição do 'statsCard' com base no tamanho da janela.
     */
    private void updateCardSize() {
        int screenWidth = parentFrame.getWidth();
        int screenHeight = parentFrame.getHeight();

        // Proporções para o tamanho do cartão em relação à tela, ajustando para telas menores.
        double widthRatio = screenWidth < 600 ? 0.95 : (screenWidth < 1000 ? 0.85 : 0.65);
        double heightRatio = screenHeight < 500 ? 0.95 : (screenHeight < 800 ? 0.85 : 0.75);

        int cardW = (int) (screenWidth * widthRatio);
        int cardH = (int) (screenHeight * heightRatio);

        // Limites mínimo e máximo para o tamanho do cartão, para evitar que fique muito pequeno ou grande.
        cardW = Math.max(320, Math.min(780, cardW));
        cardH = Math.max(400, Math.min(720, cardH));

        // Define a nova posição (centralizada) e tamanho do cartão.
        statsCard.setBounds((screenWidth - cardW) / 2, (screenHeight - cardH) / 2, cardW, cardH);
        statsCard.setPreferredSize(new Dimension(cardW, cardH));
        statsCard.setMaximumSize(new Dimension(cardW, cardH));
    }

    /**
     * Atualiza elementos responsivos, como fontes e o gráfico de distribuição, após um redimensionamento.
     */
    private void updateResponsiveElements() {
        if (!uiInitialized) return;

        SwingUtilities.invokeLater(() -> {
            // Percorre os componentes para atualizar as fontes.
            for (Component comp : statsCard.getComponents()) {
                if (comp instanceof JPanel) {
                    updatePanelFonts((JPanel) comp);
                }
            }

            rebuildDistribution(); // Recria o gráfico com os novos tamanhos.
            statsCard.revalidate();
            statsCard.repaint();
        });
    }

    /**
     * Percorre um painel recursivamente para atualizar o tamanho da fonte das JLabels.
     * @param panel O painel a ser percorrido.
     */
    private void updatePanelFonts(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Font currentFont = label.getFont();
                if (currentFont != null) {
                    // Calcula o novo tamanho da fonte com base na largura da janela.
                    int newSize = Math.max(12, Math.min(28, parentFrame.getWidth() / 40));
                    // Mantém o estilo (negrito, etc.) da fonte original.
                    label.setFont(new Font(currentFont.getName(), currentFont.getStyle(), newSize));
                }
            } else if (comp instanceof JPanel) {
                updatePanelFonts((JPanel) comp); // Chamada recursiva para painéis aninhados.
            }
        }
    }

    /**
     * Cria e estiliza uma label para exibir um valor numérico de uma métrica (o número grande).
     * @param value O texto inicial da label.
     * @return A JLabel estilizada.
     */
    private JLabel makeMetricPanel(String value) {
        JLabel label = new JLabel(value, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        int fontSize = Math.max(16, Math.min(28, parentFrame.getWidth() / 40));
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Agrupa uma label de valor (grande) com uma label de texto (pequena) verticalmente.
     * @param bigLabel A label com o número.
     * @param smallText O texto da legenda.
     * @return Um JPanel contendo as duas labels.
     */
    private JPanel wrapMetric(JLabel bigLabel, String smallText) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        bigLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(bigLabel);

        int legendSize = Math.max(10, Math.min(12, parentFrame.getWidth() / 80));
        // Usa HTML para permitir quebra de linha (`\n`) e estilização mais fácil dentro da label.
        JLabel legend = new JLabel("<html><div style='text-align:center; font-size:" + legendSize + "px; color:#dcd9d9'>" + smallText.replace("\n", "<br>") + "</div></html>", JLabel.CENTER);
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(legend);
        return p;
    }

    /**
     * Limpa e reconstrói o gráfico de barras da distribuição de tentativas.
     */
    private void rebuildDistribution() {
        if (distributionContainer == null) return;
        distributionContainer.removeAll();

        int[] dist = perfilJogador.getDistribuicaoTentativas();
        // Encontra o valor máximo para dimensionar as barras proporcionalmente.
        int max = 1; // Começa com 1 para evitar divisão por zero.
        for (int value : dist) {
            if (value > max) max = value;
        }

        // Cria uma linha para cada número de tentativas (1 a 6).
        for (int i = 0; i < 6; i++) {
            int count = dist[i];
            distributionContainer.add(makeDistributionRow(String.valueOf(i + 1), count, max));
            distributionContainer.add(Box.createRigidArea(new Dimension(0, 8))); // Espaçamento entre as barras.
        }
        // Adiciona a linha para derrotas (índice 6), representada por um ícone de caveira.
        distributionContainer.add(makeDistributionRow("\u2620", dist[6], max));
        distributionContainer.revalidate();
        distributionContainer.repaint();
    }

    /**
     * Cria uma única linha do gráfico de distribuição (Rótulo, Barra, Contagem).
     * @param label O rótulo da linha (ex: "1", "2", "☠").
     * @param count O número de jogos para esta linha.
     * @param maxCount O número máximo de jogos em qualquer linha (usado para dimensionar a barra).
     * @return Um JPanel representando a linha completa do gráfico.
     */
    private JPanel makeDistributionRow(String label, int count, int maxCount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        // Rótulo à esquerda (número da tentativa).
        JLabel left = new JLabel(label);
        left.setForeground(Color.WHITE);
        int labelWidth = Math.max(20, Math.min(30, parentFrame.getWidth() / 40));
        int rowHeight = Math.max(20, Math.min(24, parentFrame.getHeight() / 30));
        left.setPreferredSize(new Dimension(labelWidth, rowHeight));
        row.add(left, BorderLayout.WEST);

        // Painel de fundo da barra.
        JPanel barBg = new JPanel(new BorderLayout());
        barBg.setBackground(Color.decode("#312a2c"));
        int maxBarWidth = Math.max(200, Math.min(420, parentFrame.getWidth() - 200));
        barBg.setPreferredSize(new Dimension(maxBarWidth, rowHeight));
        barBg.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        // A barra de progresso real, cuja largura é proporcional à contagem ('count').
        int w = (maxCount == 0) ? 0 : (int) ((maxBarWidth * (double) count) / maxCount);
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(w, rowHeight - 6));
        bar.setBackground(Color.decode("#049CFF"));
        barBg.add(bar, BorderLayout.WEST);

        row.add(barBg, BorderLayout.CENTER);

        // Rótulo à direita (a contagem numérica).
        JLabel right = new JLabel(String.valueOf(count));
        right.setForeground(Color.WHITE);
        right.setPreferredSize(new Dimension(Math.max(30, Math.min(40, parentFrame.getWidth() / 30)), rowHeight));
        right.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }
}