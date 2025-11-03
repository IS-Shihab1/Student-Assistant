package com.studenthub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AssignmentUI extends BaseUI {
    private JPanel panel;
    private JTextField titleField;
    private JLabel fileLabel;
    private File chosenFile;
    private DefaultListModel<UploadEntry> listModel;
    private JList<UploadEntry> uploadsList;

    public AssignmentUI(String username) {
        super(username);
    panel = new JPanel(new GridBagLayout());
    // set a pleasant background for the upload area (matches app theme)
    Color bg = new Color(112, 178, 178);
    panel.setBackground(bg);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Title:"), c);
        c.gridx = 1; titleField = new JTextField(20); panel.add(titleField, c);
        c.gridx = 0; c.gridy = 1; JButton choose = new JButton("Choose File (img/pdf)"); panel.add(choose, c);
        c.gridx = 1; fileLabel = new JLabel("No file"); panel.add(fileLabel, c);
        c.gridx = 0; c.gridy = 2; JButton upload = new JButton("Upload"); panel.add(upload, c);

        choose.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int ret = fc.showOpenDialog(panel);
            if(ret == JFileChooser.APPROVE_OPTION) {
                chosenFile = fc.getSelectedFile();
                fileLabel.setText(chosenFile.getName());
            }
        });

        upload.addActionListener(e -> doUpload());

        // section: list of previous uploads
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; c.fill = GridBagConstraints.BOTH; c.weightx = 1.0; c.weighty = 1.0;
    listModel = new DefaultListModel<>();
    uploadsList = new JList<>(listModel);
        uploadsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    uploadsList.setBackground(bg);
        // Render each row with padding and a bottom divider so rows look like separate items
        uploadsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // value is UploadEntry; display text is the formatted display
                String text = value == null ? "" : ((UploadEntry) value).display;
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
                return lbl;
            }
        });
    JScrollPane sp = new JScrollPane(uploadsList);
    // ensure scroll viewport matches
    sp.getViewport().setBackground(bg);
        panel.add(sp, c);

    // buttons: open / delete / refresh
    c.fill = GridBagConstraints.NONE; c.weighty = 0; c.gridy = 4; c.gridwidth = 1;
    c.gridx = 0; JButton openBtn = new JButton("Open Selected"); panel.add(openBtn, c);
    c.gridx = 1; JButton deleteBtn = new JButton("Delete Selected"); panel.add(deleteBtn, c);
    c.gridx = 2; JButton refreshBtn = new JButton("Refresh List"); panel.add(refreshBtn, c);

        openBtn.addActionListener(e -> {
            UploadEntry sel = uploadsList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(panel, "Select an uploaded item first"); return; }
            // expected format in raw: username|title|filename|date
            String[] parts = sel.raw.split("\\|");
            if (parts.length < 3) { JOptionPane.showMessageDialog(panel, "Invalid entry format"); return; }
            String fileName = parts[2];
            File f = new File(Utils.getUploadsFolder(), fileName);
            if (!f.exists()) { JOptionPane.showMessageDialog(panel, "File not found: " + f.getAbsolutePath()); return; }
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    java.awt.Desktop.getDesktop().open(f);
                } else {
                    JOptionPane.showMessageDialog(panel, "Opening files is not supported on this platform.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to open file: " + ex.getMessage());
            }
        });

        // delete action
        deleteBtn.addActionListener(e -> {
            UploadEntry sel = uploadsList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(panel, "Select an uploaded item first"); return; }
            int conf = JOptionPane.showConfirmDialog(panel, "Delete selected entry and its file? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
            String[] parts = sel.raw.split("\\|");
            if (parts.length < 3) { JOptionPane.showMessageDialog(panel, "Invalid entry format"); return; }
            String fileName = parts[2];
            File f = new File(Utils.getUploadsFolder(), fileName);
            boolean deletedFile = true;
            if (f.exists()) {
                deletedFile = f.delete();
                if (!deletedFile) {
                    int choice = JOptionPane.showConfirmDialog(panel, "Failed to delete file from disk. Remove entry from list anyway?", "Delete Failed", JOptionPane.YES_NO_OPTION);
                    if (choice != JOptionPane.YES_OPTION) return;
                }
            }
            // remove the line from assignments.txt
            try {
                File af = new File(Utils.DATA_FOLDER, "assignments.txt");
                java.util.List<String> lines = Utils.readAllLines(af);
                boolean removed = lines.remove(sel.raw);
                if (removed) {
                    String content = String.join(System.lineSeparator(), lines);
                    Utils.writeStringToFile(af, content);
                }
                loadUploads();
                JOptionPane.showMessageDialog(panel, "Deleted.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to remove entry: " + ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> loadUploads());

        // load existing uploads into the list
        loadUploads();
    }

    private void doUpload() {
        try {
            if(chosenFile == null) { JOptionPane.showMessageDialog(panel, "Choose a file first"); return; }
            String title = titleField.getText().trim();
            if(title.isEmpty()) { JOptionPane.showMessageDialog(panel, "Enter title"); return; }
            // copy file to uploads
            File uploads = Utils.getUploadsFolder();
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String destName = username + "_" + timestamp + "_" + chosenFile.getName();
            File dest = new File(uploads, destName);
            Files.copy(chosenFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String line = String.join("|", username, title, dest.getName(), new Date().toString());
            Utils.appendLine(new File(Utils.DATA_FOLDER, "assignments.txt"), line);
            JOptionPane.showMessageDialog(panel, "Uploaded");
            // Offer to open the uploaded file immediately
            int openChoice = JOptionPane.showConfirmDialog(panel, "Open uploaded file now?", "Open File", JOptionPane.YES_NO_OPTION);
            if (openChoice == JOptionPane.YES_OPTION) {
                try {
                    if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                        java.awt.Desktop.getDesktop().open(dest);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Opening files is not supported on this platform.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Failed to open file: " + ex.getMessage());
                }
            }
            // refresh the uploads list and select the newly added entry
            loadUploads();
            // try to select the last entry which should be the one we just added
            if (listModel.size() > 0) {
                uploadsList.setSelectedIndex(listModel.size() - 1);
                uploadsList.ensureIndexIsVisible(listModel.size() - 1);
            }
            titleField.setText(""); fileLabel.setText("No file"); chosenFile = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Upload failed: " + ex.getMessage());
        }
    }

    private void loadUploads() {
        listModel.clear();
        try {
            File f = new File(Utils.DATA_FOLDER, "assignments.txt");
            List<String> lines = Utils.readAllLines(f);
            for (String l : lines) {
                if (l == null || l.trim().isEmpty()) continue;
                // parse and format for display: username|title|filename|date
                String display = l;
                try {
                    String[] parts = l.split("\\|");
                    String user = parts.length > 0 ? parts[0] : "";
                    String title = parts.length > 1 ? parts[1] : "";
                    String fname = parts.length > 2 ? parts[2] : "";
                    String date = parts.length > 3 ? parts[3] : "";
                    display = String.format("%s — %s — %s", title, fname, date);
                } catch (Exception ignored) {}
                listModel.addElement(new UploadEntry(l, display));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // small helper to keep raw line and formatted display together
    private static class UploadEntry {
        final String raw;
        final String display;
        UploadEntry(String raw, String display) { this.raw = raw; this.display = display; }
        @Override public String toString() { return display; }
    }

    @Override
    public JPanel getPanel() { return panel; }
}
