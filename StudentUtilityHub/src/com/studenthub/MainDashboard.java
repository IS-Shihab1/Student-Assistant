package com.studenthub;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private String username;
    private JPanel contentArea;

    public MainDashboard(String username) {
        this.username = username;
        setTitle("Student Assistant - " + username);
        // set title font size larger
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel left = new JPanel();
        left.setBackground(Color.LIGHT_GRAY);
        left.setLayout(new GridLayout(6,1,6,6));
        JButton btnAssign = new JButton("Assignments & Notes");
        JButton btnQuiz = new JButton("Offline Quiz");
       
        JButton btnTime = new JButton("Daily Time Sheet");
        JButton btnExpense = new JButton("Expense Tracker");
        JButton btnTodo = new JButton("To-Do List");
        JButton btnLogout = new JButton("Logout");



        // coloring buttons---------------------------///
        btnAssign.setBackground(new Color(112, 178, 178));
        btnAssign.setForeground(Color.WHITE);
        btnQuiz.setBackground(new Color(176, 206, 136));
        btnQuiz.setForeground(Color.WHITE);
        btnExpense.setBackground(new Color(0, 0, 0));
        btnExpense.setForeground(Color.WHITE);
        btnTime.setBackground(new Color(255, 198, 157));
        btnTime.setForeground(Color.WHITE);
        btnTodo.setBackground(new Color(230, 245, 255));
        btnTodo.setForeground(Color.BLACK);
        btnLogout.setBackground(new Color(0, 0, 0));
        btnLogout.setForeground(Color.WHITE);
        //-----------------------------------------/////


        left.add(btnAssign); left.add(btnQuiz);  left.add(btnTime); left.add(btnTodo);left.add(btnExpense); left.add(btnLogout);

        // load background image for welcome area
        Image welcomeBg = loadBackgroundImage("Learning-Methods.png");
        contentArea = new BackgroundPanel(welcomeBg);
        contentArea.setLayout(new BorderLayout());

        // welcome label visible at top and centered
        JLabel welcome = new JLabel("Welcome to Student Assistant, " + username);
        welcome.setFont(new Font("Arial", Font.BOLD, 28));
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        welcome.setForeground(Color.WHITE);

        // top bar with translucent background to ensure text contrast over image
        // Use a full-width opaque panel with a translucent background so the welcome text
        // remains readable regardless of the background image colors.
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(true);
        topBar.setBackground(new Color(255, 0, 0, 120)); // translucent red
        topBar.setPreferredSize(new Dimension(0, 80));

        // Style the welcome label for contrast and spacing
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        welcome.setOpaque(false); // keep label transparent; topBar provides contrast
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        welcome.setForeground(Color.WHITE);

        topBar.add(welcome, BorderLayout.CENTER);
        contentArea.add(topBar, BorderLayout.NORTH);

        // keep an empty center panel for initial content (welcome background covers full area visually)
        JPanel emptyCenter = new JPanel();
        emptyCenter.setOpaque(false);
        contentArea.add(emptyCenter, BorderLayout.CENTER);
    
        ///-----------------------------------------/////
    
    
        btnAssign.addActionListener(e -> switchTo(new AssignmentUI(username)));
        btnQuiz.addActionListener(e -> switchTo(new QuizUI(username)));
        btnExpense.addActionListener(e -> switchTo(new ExpenseTrackerUI(username)));
        btnTime.addActionListener(e -> switchTo(new DailyTimeUI(username)));
        btnTodo.addActionListener(e -> switchTo(new TodoUI(username)));
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame(); });

        getContentPane().setLayout(new BorderLayout(8,8));
        getContentPane().add(left, BorderLayout.WEST);
        getContentPane().add(contentArea, BorderLayout.CENTER);

        setVisible(true);
    }

    private void switchTo(BaseUI ui) {
        contentArea.removeAll();
        contentArea.add(ui.getPanel(), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // load a background image for the dashboard welcome area
    private Image loadBackgroundImage(String name) {
        // try classpath first
        try {
            java.net.URL u = getClass().getResource('/' + name);
            if (u != null) return new ImageIcon(u).getImage();
        } catch (Exception ignored) {}
        // project-relative locations
        String[] candidates = new String[]{"resources/" + name, name};
        for (String c : candidates) {
            try {
                java.io.File f = new java.io.File(c);
                if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
            } catch (Exception ignored) {}
        }
        // fallback: search for files containing 'learning' and 'methods'
        try {
            java.io.File root = new java.io.File(".");
            final java.io.File[] found = {null};
            java.nio.file.Files.walk(root.toPath())
                    .filter(java.nio.file.Files::isRegularFile)
                    .forEach(p -> {
                        if (found[0] != null) return;
                        String n = p.getFileName().toString().toLowerCase();
                        if (n.contains("learning") && n.contains("method")) {
                            found[0] = p.toFile();
                        }
                    });
            if (found[0] != null) return new ImageIcon(found[0].getAbsolutePath()).getImage();
        } catch (Exception ignored) {}
        return null;
    }

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) { this.bg = bg; setLayout(new BorderLayout()); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(new Color(230,245,255)); g.fillRect(0,0,getWidth(),getHeight()); }
        }
    }
}
