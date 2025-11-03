package com.studenthub;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;

public class DailyTimeUI extends BaseUI {
    private JPanel panel;
    private DefaultTableModel model;
    private String monthFileName;
    private DefaultListModel<String> monthsModel;
    private JList<String> monthsList;

    public DailyTimeUI(String username) {
        super(username);
        panel = new JPanel(new BorderLayout(6,6));

        //------panel coloring---------/////
        panel.setBackground(new Color(255,229,204));
        //----------------------------/////


        model = new DefaultTableModel(new String[]{"Date","School","Study","Play","Mobile"}, 0);
        
        JTable table = new JTable(model);
        ///-----table coloring---------/////
        table.setBackground(new Color(255, 229, 204));
        //----------------------------/////

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // right-side panel: list of saved month sheets
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        right.setBackground(new Color(255,229,204));
        right.add(new JLabel("Saved Months:"), BorderLayout.NORTH);
        monthsModel = new DefaultListModel<>();
        monthsList = new JList<>(monthsModel);
        monthsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        monthsList.setVisibleRowCount(10);
        monthsList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
                return lbl;
            }
        });
        right.add(new JScrollPane(monthsList), BorderLayout.CENTER);
        JPanel rightBtns = new JPanel(new GridLayout(3,1,6,6));
        rightBtns.setBackground(new Color(255,229,204));
        JButton openMonth = new JButton("Open");
        JButton refreshMonths = new JButton("Refresh");
        JButton deleteMonth = new JButton("Delete");
        rightBtns.add(openMonth); rightBtns.add(refreshMonths); rightBtns.add(deleteMonth);
        right.add(rightBtns, BorderLayout.SOUTH);

        panel.add(right, BorderLayout.EAST);
    JPanel bottom = new JPanel();

        //-----bottom panel coloring---------/////
        bottom.setBackground(new Color(255,229,204));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        /////----------------------------/////
        JButton addRow = new JButton("Add Row");
        JButton deleteRow = new JButton("Delete Row");
        JButton save = new JButton("Save Month");
        bottom.add(addRow); bottom.add(deleteRow); bottom.add(save);
        panel.add(bottom, BorderLayout.SOUTH);

        addRow.addActionListener(e -> model.addRow(new Object[]{new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()),"0","0","0","0"}));
        deleteRow.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                model.removeRow(sel);
            } else {
                JOptionPane.showMessageDialog(panel, "Select a row to delete");
            }
        });

        save.addActionListener(e -> saveMonth());

        // months list actions
        refreshMonths.addActionListener(e -> loadMonths());
        openMonth.addActionListener(e -> {
            String sel = monthsList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(panel, "Select a month file first"); return; }
            File f = new File(Utils.DATA_FOLDER, sel);
            if (!f.exists()) { JOptionPane.showMessageDialog(panel, "File not found: " + f.getAbsolutePath()); return; }
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    java.awt.Desktop.getDesktop().open(f);
                } else {
                    JOptionPane.showMessageDialog(panel, "Opening files is not supported on this platform.");
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(panel, "Failed to open file: " + ex.getMessage()); }
        });
        deleteMonth.addActionListener(e -> {
            String sel = monthsList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(panel, "Select a month file first"); return; }
            int conf = JOptionPane.showConfirmDialog(panel, "Delete selected month file?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
            File f = new File(Utils.DATA_FOLDER, sel);
            if (f.exists()) {
                if (!f.delete()) {
                    JOptionPane.showMessageDialog(panel, "Failed to delete file: " + f.getAbsolutePath());
                }
            }
            loadMonths();
        });

        monthFileName = username + "_" + new SimpleDateFormat("yyyy_MM").format(new java.util.Date()) + "_time.csv";
        // load existing month files for this user
        loadMonths();
    }

    private void saveMonth() {
        try {
            StringBuilder sb = new StringBuilder();
            for(int r=0;r<model.getRowCount();r++){
                for(int c=0;c<model.getColumnCount();c++){
                    sb.append(model.getValueAt(r,c));
                    if(c<model.getColumnCount()-1) sb.append(",");
                }
                sb.append("\n");
            }
            Utils.writeStringToFile(new File(Utils.DATA_FOLDER, monthFileName), sb.toString());
            JOptionPane.showMessageDialog(panel, "Saved to data/"+monthFileName);
            // refresh month list and select the saved file
            loadMonths();
            monthsList.setSelectedValue(monthFileName, true);
        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(panel, "Save failed"); }
    }

    private void loadMonths() {
        monthsModel.clear();
        try {
            File df = new File(Utils.DATA_FOLDER);
            if (!df.exists()) return;
            File[] files = df.listFiles((dir, name) -> name.endsWith("_time.csv") && name.startsWith(username + "_"));
            if (files != null) {
                java.util.Arrays.sort(files);
                for (File f : files) monthsModel.addElement(f.getName());
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @Override
    public JPanel getPanel() { return panel; }
}
