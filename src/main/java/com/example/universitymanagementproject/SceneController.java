package com.example.universitymanagementproject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SceneController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    // Method to set the welcome message and role
    public void setUserData(String username, String role) {
        welcomeLabel.setText("Welcome, " + username + "!");
        roleLabel.setText("Role: " + role);
    }
}