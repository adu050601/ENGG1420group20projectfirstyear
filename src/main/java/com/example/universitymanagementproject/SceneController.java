package com.example.universitymanagementproject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

public class SceneController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private TabPane tabPane;

    // Method to set the welcome message and role
    public void setUserData(String username, String role) {
        welcomeLabel.setText("Welcome, 123456" + username + "!");
        roleLabel.setText("Role: " + role);

        // 存储用户数据作为TabPane的属性
        if (tabPane != null) {
            tabPane.getProperties().put("currentUsername", username);
            tabPane.getProperties().put("currentRole", role);
        }
    }
}