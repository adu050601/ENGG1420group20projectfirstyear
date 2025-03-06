package com.example.universitymanagementproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UniversityManagementProject extends Application {

    // Maps to store user credentials
    private static final Map<String, String> studentCredentials = new HashMap<>();
    private static final Map<String, String> facultyCredentials = new HashMap<>();

    // Hardcoded ADMIN credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        // Load data from Excel file
        loadUserData();
        // Launch the JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Management System - Login");

        // Create the login form
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        // Username field
        Label userLabel = new Label("Username:");
        GridPane.setConstraints(userLabel, 0, 0);
        TextField userInput = new TextField();
        userInput.setPromptText("Enter username");
        GridPane.setConstraints(userInput, 1, 0);

        // Password field
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 1);
        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Enter password");
        GridPane.setConstraints(passInput, 1, 1);

        // Login button
        Button loginButton = new Button("Login");
        GridPane.setConstraints(loginButton, 1, 2);

        // Status label
        Label statusLabel = new Label();
        GridPane.setConstraints(statusLabel, 1, 3);

        // Add components to the grid
        grid.getChildren().addAll(userLabel, userInput, passLabel, passInput, loginButton, statusLabel);

        // Login button action
        loginButton.setOnAction(e -> {
            String username = userInput.getText();
            String password = passInput.getText();

            // Check ADMIN credentials
            if (username.equals(ADMIN_USERNAME)) {
                if (password.equals(ADMIN_PASSWORD)) {
                    statusLabel.setText("Login successful! Welcome ADMIN.");
                    showAdminDashboard(username, "ADMIN");
                } else {
                    statusLabel.setText("Incorrect password for ADMIN.");
                }
            }
            // Check student credentials
            else if (studentCredentials.containsKey(username)) {
                if (studentCredentials.get(username).equals(password)) {
                    statusLabel.setText("Login successful! Welcome Student: " + username);
                    showStudentDashboard(username, "Student");
                } else {
                    statusLabel.setText("Incorrect password for student.");
                }
            }
            // Check faculty credentials
            else if (facultyCredentials.containsKey(username)) {
                if (facultyCredentials.get(username).equals(password)) {
                    statusLabel.setText("Login successful! Welcome Faculty: " + username);
                    showFacultyDashboard(username, "Faculty");
                } else {
                    statusLabel.setText("Incorrect password for faculty.");
                }
            }
            // Invalid username
            else {
                statusLabel.setText("Invalid username.");
            }
        });

        // Set up the scene
        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to load user data from Excel file
    private static void loadUserData() {
        try (FileInputStream file = new FileInputStream("UMS_Data.xlsx")) {
            Workbook workbook = new XSSFWorkbook(file);

            // Load student credentials
            Sheet studentSheet = workbook.getSheetAt(2); // Students sheet (index 2)
            for (Row row : studentSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                // Check if cells exist before accessing them
                Cell studentIDCell = row.getCell(0);
                Cell passwordCell = row.getCell(11);

                if (studentIDCell != null && passwordCell != null) {
                    String studentID = studentIDCell.getStringCellValue();
                    String password = passwordCell.getStringCellValue();
                    studentCredentials.put(studentID, password);
                }
            }

            // Load faculty credentials
            Sheet facultySheet = workbook.getSheetAt(3); // Faculties sheet (index 3)
            for (Row row : facultySheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                // Check if cells exist before accessing them
                Cell facultyIDCell = row.getCell(0);
                Cell passwordCell = row.getCell(7);

                if (facultyIDCell != null && passwordCell != null) {
                    String facultyID = facultyIDCell.getStringCellValue();
                    String password = passwordCell.getStringCellValue();
                    facultyCredentials.put(facultyID, password);
                }
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to show ADMIN dashboard
    private void showAdminDashboard(String username, String role) {
        Stage adminStage = new Stage();
        adminStage.setTitle("Admin Dashboard");

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementproject/hello-view.fxml"));
            BorderPane root = loader.load();

            // Get the controller and set user data
            SceneController controller = loader.getController();
            controller.setUserData(username, role);

            // Set the scene
            Scene scene = new Scene(root, 800, 600);
            adminStage.setScene(scene);
            adminStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to show student dashboard
    private void showStudentDashboard(String username, String role) {
        Stage studentStage = new Stage();
        studentStage.setTitle("Student Dashboard");

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementproject/student-view.fxml"));
            BorderPane root = loader.load();

            // Get the controller and set user data
            SceneController controller = loader.getController();
            controller.setUserData(username, role);

            // Set the scene
            Scene scene = new Scene(root, 800, 600);
            studentStage.setScene(scene);
            studentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to show faculty dashboard
    private void showFacultyDashboard(String username, String role) {
        Stage facultyStage = new Stage();
        facultyStage.setTitle("Faculty Dashboard");

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementproject/student-view.fxml"));
            BorderPane root = loader.load();

            // Get the controller and set user data
            SceneController controller = loader.getController();
            controller.setUserData(username, role);

            // Set the scene
            Scene scene = new Scene(root, 800, 600);
            facultyStage.setScene(scene);
            facultyStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}