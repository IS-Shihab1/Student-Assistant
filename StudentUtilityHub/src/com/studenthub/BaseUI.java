package com.studenthub;

import javax.swing.*;

public abstract class BaseUI {
    protected String username;
    public BaseUI(String username) { this.username = username; }
    public abstract JPanel getPanel();
}
