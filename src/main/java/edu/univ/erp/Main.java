package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

     
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        
        SwingUtilities.invokeLater(() -> {
            LoginUI login = new LoginUI();
            login.setVisible(true);
        });
    }
}
