package com.studenthub;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Ensure needed folders and seed files
        Utils.ensureAppFolders();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}
