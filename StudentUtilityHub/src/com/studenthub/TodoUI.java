package com.studenthub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class TodoUI extends BaseUI {
    private JPanel panel;
    private DefaultListModel<String> listModel;

    public TodoUI(String username) {
        super(username);
    panel = new JPanel(new BorderLayout(6,6));
    Color bg = new Color(230, 245, 255);
    panel.setBackground(bg);
        listModel = new DefaultListModel<>();
    JList<String> list = new JList<>(listModel);
    list.setBackground(bg);
    // render each todo with padding and a divider line so rows are visually separated
    list.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = value == null ? "" : value.toString();
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            return lbl;
        }
    });
    JScrollPane jsp = new JScrollPane(list);
    jsp.getViewport().setBackground(bg);
    panel.add(jsp, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
    bottom.setBackground(bg);
    JTextField in = new JTextField(20);
    JButton add = new JButton("Add");
    JButton edit = new JButton("Edit");
    JButton del = new JButton("Delete");
    bottom.add(in); bottom.add(add); bottom.add(edit); bottom.add(del);
        panel.add(bottom, BorderLayout.SOUTH);

        // load existing
        File f = new File(Utils.DATA_FOLDER, "todo.txt");
        List<String> lines = Utils.readAllLines(f);
        for(String l: lines) if(!l.trim().isEmpty()) listModel.addElement(l);

        add.addActionListener(e -> {
            String t = in.getText().trim(); if(t.isEmpty()) return;
            listModel.addElement(t);
            Utils.appendLine(new File(Utils.DATA_FOLDER, "todo.txt"), t);
            in.setText("");
            // ensure new item is visible and selected
            int last = listModel.size()-1; if (last >= 0) { list.setSelectedIndex(last); list.ensureIndexIsVisible(last); }
        });
        edit.addActionListener(e -> {
            int ix = list.getSelectedIndex();
            if (ix < 0) { JOptionPane.showMessageDialog(panel, "Select an item to edit"); return; }
            String old = listModel.get(ix);
            String updated = (String) JOptionPane.showInputDialog(panel, "Edit todo:", "Edit", JOptionPane.PLAIN_MESSAGE, null, null, old);
            if (updated != null) {
                updated = updated.trim();
                if (!updated.isEmpty()) {
                    listModel.set(ix, updated);
                    Utils.saveCollectionToFile(listModel, new File(Utils.DATA_FOLDER, "todo.txt"));
                    list.setSelectedIndex(ix);
                }
            }
        });
        del.addActionListener(e -> {
            int ix = list.getSelectedIndex(); if(ix>=0){ listModel.remove(ix); Utils.saveCollectionToFile(listModel, new File(Utils.DATA_FOLDER, "todo.txt")); }
        });
    }

    @Override
    public JPanel getPanel() { return panel; }
}
