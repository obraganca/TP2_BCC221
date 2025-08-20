package com.termo.gui;
import javax.swing.*;
import java.awt.*;
import com.termo.controller.*;

public class LoginScreen extends JDialog {
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private Usuario usuarioAutenticado;

    public LoginScreen(JFrame parent, Login sistemaLogin) {
        super(parent, "Login", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userLabel = new JLabel("Usuário:");
        JLabel passLabel = new JLabel("Senha:");
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        loginButton = new JButton("Entrar");

        gbc.gridx = 0; gbc.gridy = 0; add(userLabel, gbc);
        gbc.gridx = 1; add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; add(passLabel, gbc);
        gbc.gridx = 1; add(passField, gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String nome = userField.getText();
            String senha = new String(passField.getPassword());

            if (sistemaLogin.loginOuCadastrar(nome, senha)) {
                usuarioAutenticado = sistemaLogin.getUsuario(nome);
                JOptionPane.showMessageDialog(this,
                        "Bem-vindo, " + nome + "!");
                dispose(); // fecha a tela de login
            } else {
                JOptionPane.showMessageDialog(this,
                        "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
