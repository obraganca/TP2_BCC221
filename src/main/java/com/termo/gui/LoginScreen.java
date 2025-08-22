package com.termo.gui;

import javax.swing.*;
import java.awt.*;
import com.termo.controller.*;

/**
 * Representa a tela de login como uma caixa de diálogo.
 * Esta classe é responsável por obter as credenciais do usuário e validar com o sistema de login.
 */
public class LoginScreen extends JDialog {
    // Campos de texto para o nome de usuário e senha.
    private JTextField userField;
    private JPasswordField passField;
    // Botão para iniciar a tentativa de login.
    private JButton loginButton;
    // Armazena o usuário após a autenticação bem-sucedida.
    private Usuario usuarioAutenticado;

    /**
     * Construtor da tela de login.
     * @param parent A janela principal sobre a qual este diálogo será exibido.
     * @param sistemaLogin A instância do controller de login para autenticação.
     */
    public LoginScreen(JFrame parent, Login sistemaLogin) {
        // Chama o construtor da superclasse JDialog, definindo o título e a modalidade.
        super(parent, "Login", true);
        // Define o layout como GridBagLayout para um posicionamento flexível dos componentes.
        setLayout(new GridBagLayout());
        // GridBagConstraints é usado para configurar a posição e o tamanho dos componentes no layout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Adiciona um espaçamento (margem) ao redor dos componentes.

        // Cria os componentes da interface gráfica.
        JLabel userLabel = new JLabel("Usuário:");
        JLabel passLabel = new JLabel("Senha:");
        userField = new JTextField(15); // Campo de texto com largura para 15 caracteres.
        passField = new JPasswordField(15); // Campo de senha que oculta os caracteres.
        loginButton = new JButton("Entrar");

        // Adiciona os componentes à tela usando GridBagConstraints para posicioná-los.
        gbc.gridx = 0; gbc.gridy = 0; add(userLabel, gbc); // Posição (0,0)
        gbc.gridx = 1; add(userField, gbc);               // Posição (1,0)
        gbc.gridx = 0; gbc.gridy = 1; add(passLabel, gbc); // Posição (0,1)
        gbc.gridx = 1; add(passField, gbc);               // Posição (1,1)
        gbc.gridx = 1; gbc.gridy = 2; add(loginButton, gbc); // Posição (1,2)

        // Adiciona um ActionListener ao botão de login para tratar o evento de clique.
        loginButton.addActionListener(e -> {
            // Obtém o nome de usuário e a senha dos campos de texto.
            String nome = userField.getText();
            String senha = new String(passField.getPassword());

            // Tenta realizar o login ou cadastro com as credenciais fornecidas.
            if (sistemaLogin.loginOuCadastrar(nome, senha)) {
                // Se o login for bem-sucedido, armazena o objeto do usuário.
                usuarioAutenticado = sistemaLogin.getUsuario(nome);
                // Exibe uma mensagem de boas-vindas.
                JOptionPane.showMessageDialog(this,
                        "Bem-vindo, " + nome + "!");
                dispose(); // Fecha a tela de login.
            } else {
                // Se a senha estiver incorreta, exibe uma mensagem de erro.
                JOptionPane.showMessageDialog(this,
                        "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ajusta o tamanho da janela para caber todos os componentes.
        pack();
        // Centraliza a janela de login em relação à janela pai.
        setLocationRelativeTo(parent);
    }

    /**
     * Método público para obter o usuário que foi autenticado.
     * @return O objeto Usuario se a autenticação foi bem-sucedida, caso contrário, null.
     */
    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}