package com.studenthub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Student Utility Hub - Login");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    // prefer a reasonable default size and let pack() adjust to content
    setPreferredSize(new Dimension(400, 360));

        // try to load the app logo image from common locations (classpath first, then project folders)
        Image logo = loadLogoImage();
        if (logo != null) setIconImage(logo);

        BackgroundPanel p = new BackgroundPanel(logo);
        p.setBackground(new Color(7, 213, 245, 1));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Username:"), c);
    c.gridx = 1; usernameField = new JTextField(16); usernameField.setBackground(Color.WHITE); p.add(usernameField, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Password:"), c);
    c.gridx = 1; passwordField = new JPasswordField(16); passwordField.setBackground(Color.WHITE); p.add(passwordField, c);

        JButton loginBtn = new JButton("Login");
        JButton signBtn = new JButton("Sign Up");
        loginBtn.setBackground(new Color (10, 218, 211, 1));
        c.gridx = 0; c.gridy = 2; p.add(loginBtn, c);
        c.gridx = 1; p.add(signBtn, c);

        loginBtn.addActionListener(e -> doLogin());
        signBtn.addActionListener(e -> doSignup());

        add(p);
        // let layout compute sizes based on preferred sizes, then center on screen
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // Try several common locations to load an image named "app logo.png" or "applogo.png".
    private Image loadLogoImage() {
        String[] candidates = new String[]{"/app logo.png", "/applogo.png", "/app_logo.png", "/images/app logo.png", "/images/applogo.png", "resources/app logo.png", "resources/applogo.png", "app logo.png", "applogo.png"};
        for (String c : candidates) {
            try {
                // try classpath first (leading slash)
                if (c.startsWith("/")) {
                    java.net.URL u = getClass().getResource(c);
                    if (u != null) return new ImageIcon(u).getImage();
                }
                // fallback to file-system relative to project
                java.io.File f = new java.io.File(c);
                if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
                // try under resources folder relative path
                java.io.File f2 = new java.io.File("resources" + java.io.File.separator + c.replaceFirst(".*/", ""));
                if (f2.exists()) return new ImageIcon(f2.getAbsolutePath()).getImage();
            } catch (Exception ignored) {}
        }
        // If not found yet, try a recursive search in the project directory for any file
        // whose name contains both "app" and "logo" (case-insensitive) and ends with png/jpg/jpeg.
        try {
            java.io.File projectRoot = new java.io.File(".");
            final java.io.File[] found = {null};
            java.nio.file.Files.walk(projectRoot.toPath())
                    .filter(java.nio.file.Files::isRegularFile)
                    .forEach(p -> {
                        if (found[0] != null) return;
                        String name = p.getFileName().toString().toLowerCase();
                        if ((name.contains("app") && name.contains("logo")) && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
                            found[0] = p.toFile();
                        }
                    });
            if (found[0] != null) return new ImageIcon(found[0].getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        return null;
    }

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) {
            this.bg = bg;
            setLayout(new GridBagLayout());
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                // fallback background color
                g.setColor(new Color(230, 245, 255));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        }
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if(user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter username & password"); return; }
        boolean ok = Utils.checkCredentials(user, pass);
        if(ok) {
            dispose();
            new MainDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials");
        }
    }

    private void doSignup() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if(user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter username & password"); return; }
        boolean created = Utils.createUser(user, pass);
        if(created) JOptionPane.showMessageDialog(this, "Account created. You can now login.");
        else JOptionPane.showMessageDialog(this, "User already exists.");
    }
}
