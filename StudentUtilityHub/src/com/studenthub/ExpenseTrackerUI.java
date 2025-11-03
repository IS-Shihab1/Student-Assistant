package com.studenthub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExpenseTrackerUI extends BaseUI {
    private JPanel panel;
    private JTextField amountField;
    private JComboBox<String> typeBox;
    private JTextField categoryField;

    public ExpenseTrackerUI(String username) {
        super(username);
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6);
        c.gridx=0; c.gridy=0; panel.add(new JLabel("Amount:"), c);
        c.gridx=1; amountField = new JTextField(10); panel.add(amountField, c);
        c.gridx=0; c.gridy=1; panel.add(new JLabel("Type:"), c);
        c.gridx=1; typeBox = new JComboBox<>(new String[]{"Expense","Income"}); panel.add(typeBox, c);
        c.gridx=0; c.gridy=2; panel.add(new JLabel("Category:"), c);
        c.gridx=1; categoryField = new JTextField(12); panel.add(categoryField, c);
        c.gridx=0; c.gridy=3; JButton add = new JButton("Add"); panel.add(add, c);

        add.addActionListener(e -> addEntry());
    }

    private void addEntry() {
        try {
            double amt = Double.parseDouble(amountField.getText().trim());
            String type = (String)typeBox.getSelectedItem();
            String cat = categoryField.getText().trim();
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String line = String.join(",", username, type, String.valueOf(amt), cat, date);
            Utils.appendLine(new File(Utils.DATA_FOLDER, "expenses.csv"), line);
            JOptionPane.showMessageDialog(panel, "Saved");
            amountField.setText(""); categoryField.setText("");
        } catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Invalid amount"); }
    }

    @Override
    public JPanel getPanel() { return panel; }
}
