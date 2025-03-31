package com.example.universitymanagementproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventController {
    private EventService eventService;
    private String currentRole;
    private String currentUsername;

    //FXML elements
    //Table
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> capacityColumn;

    //Form elements
    @FXML private ImageView hearImageView;
    @FXML private TextField eventNameField;
    @FXML private TextField eventCodeField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField capacityField;
    @FXML private TextField costField;
    @FXML private ListView<String> registeredStudentsListView;
    @FXML private Label statusLabel;

    //Buttons
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button uploadImageButton;
    @FXML private Button registerButton;


    //默认图片，路径在根目录的default_header.png
    private String currentImagePath = "images/default_header.png";

    @FXML
    public void initialize() { // FXML加载后会自动调用
        System.out.println("Event Controller Initialized");
        this.eventService = new EventService(new ExcelDataManager());

        currentRole = "ADMIN";
        currentUsername = "admin";

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        loadEvents();

        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayEventDetails(newSelection);
            }
        });

        toggleControls(true);
    }

    // 设置当前用户
    public void setUserData(String username, String role) {
        this.currentRole = role;
        this.currentUsername = username;

        configureUI();
    }

    private void configureUI() {
        if (currentRole.equals("ADMIN")) {
            addButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            uploadImageButton.setVisible(true);

            eventNameField.setEditable(true);
            eventCodeField.setEditable(true);
            descriptionArea.setEditable(true);
            locationField.setEditable(true);
            datePicker.setEditable(true);
            timeField.setEditable(true);
            capacityField.setEditable(true);
            costField.setEditable(true);
        } else {
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            uploadImageButton.setVisible(false);
        }
    }

    private void toggleControls(boolean isDisabled) {
        boolean disable = !isDisabled;
        eventNameField.setDisable(disable);
        eventCodeField.setDisable(disable);
        descriptionArea.setDisable(disable);
        locationField.setDisable(disable);
        datePicker.setDisable(disable);
        timeField.setDisable(disable);
        capacityField.setDisable(disable);
        costField.setDisable(disable);
    }

    public void addEvent(Event event) {
        eventService.addEvent(event);
    }

    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    private void loadEvents(){
        ObservableList<Event> events = FXCollections.observableArrayList(eventService.getAllEvents());
        eventTable.setItems(events);
    }

    private boolean validateInputs(){
        if (eventNameField.getText().isEmpty()
                || eventCodeField.getText().isEmpty()
                || descriptionArea.getText().isEmpty()
                || locationField.getText().isEmpty()
                || datePicker.getValue() == null
                || timeField.getText().isEmpty()
                || capacityField.getText().isEmpty()
                || costField.getText().isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all the fields.");
            return false;
        }
        return true;
    }

    @FXML
    public void handleAddEvent() {
        validateInputs();

        try {
            Event event = createEventFromForm();
            if (eventService.addEvent(event)) {
                loadEvents();
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Event Added", "Event added successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed To Add Event", "Event with code " + event.getEventCode() + " already exists.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed To Add Event", "An error occurred while adding the event.");
        }
    }

    @FXML
    public void handleEditEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Edit Event Failed", "Please select an event to edit.");
            return;
        }

        validateInputs();

        try {
            Event event = createEventFromForm();
            if (eventService.updateEvent(event)) {
                loadEvents();
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Event Updated", "Event updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed To Update Event", "An error occurred while updating the event.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed To Update Event", "An error occurred while updating the event.");
        }
    }

    @FXML
    public void handleDeleteEvent() {
        System.out.println("Delete Event Button Clicked");
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Delete Event Failed", "Please select an event to delete.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this event?");
        confirmationAlert.setTitle("Delete Event");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Do you want to delete the event: " + selectedEvent.getEventName() + "?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.deleteEvent(selectedEvent.getEventCode())){
                loadEvents();
            }
        }
    }

    @FXML
    public void handleUploadImage() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Upload Image Failed", "Please select an event to upload an image.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File to Upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                Path imagesDirectory = Paths.get("images");
                if (!Files.exists(imagesDirectory)) {
                    Files.createDirectories(imagesDirectory);
                }

                String fileName = "event_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destinationPath = imagesDirectory.resolve(fileName);

                Files.copy(selectedFile.toPath(), destinationPath);

                Image image = new Image(destinationPath.toUri().toString());
                hearImageView.setImage(image);

                currentImagePath = destinationPath.toString();
                System.out.println(">>>currentImagePath: " + currentImagePath);

                String eventName = selectedEvent.getEventName();
                String eventCode = selectedEvent.getEventCode();
                String description = selectedEvent.getDescription();
                String location = selectedEvent.getLocation();
                LocalDateTime dateTime = selectedEvent.getDateTime();
                int capacity = selectedEvent.getCapacity();
                double cost = selectedEvent.getCost();

                Event updatedEvent = new Event(eventName, eventCode, description, currentImagePath, location, dateTime, capacity, cost);
                eventService.updateEvent(updatedEvent);

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Upload Image Failed", "An error occurred while uploading the image.");
            }
        }
    }

    @FXML
    public void handleRegisterEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Register Event Failed", "Please select an event to register.");
            return;
        }

        boolean isRegistered = selectedEvent.isRegistered(currentUsername);

        if (isRegistered){
            eventService.unregisterStudent(selectedEvent.getEventCode(), currentUsername);
            showAlert(Alert.AlertType.INFORMATION, "Unregistered", "You have been unregistered from the event.");
        } else {
            if (selectedEvent.isFull()) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Event is full. Cannot register.");
                return;
            } else {
                eventService.registerStudent(selectedEvent.getEventCode(), currentUsername);
                showAlert(Alert.AlertType.INFORMATION, "Registered", "You have been registered for the event.");
            }
        }

        loadEvents();
        for (Event event : eventTable.getItems()) {
            if (event.getEventCode().equals(selectedEvent.getEventCode())) {
                displayEventDetails(event);
                break;
            }
        }
    }

    private void displayEventDetails(Event event) {
        eventNameField.setText(event.getEventName());
        eventCodeField.setText(event.getEventCode());
        descriptionArea.setText(event.getDescription());
        locationField.setText(event.getLocation());
        capacityField.setText(String.valueOf(event.getCapacity()));
        costField.setText(String.valueOf(event.getCost()));

        //date and time
        datePicker.setValue(event.getDateTime().toLocalDate());
        timeField.setText(event.getDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        //registered students
        if (currentRole.equals("ADMIN")) {
            List<String> registeredStudents = event.getRegisteredStudents();
            ObservableList<String> students = FXCollections.observableArrayList(registeredStudents != null ? registeredStudents: new ArrayList<>());
            registeredStudentsListView.setItems(students);
        } else {
            boolean isRegistered = event.isRegistered(currentUsername);
            if (isRegistered) {
                registerButton.setText("Registered");
            } else {
                registerButton.setText("Click to Register");
            }
            if (event.isFull() && !isRegistered) {
                registerButton.setDisable(true);
            } else {
                registerButton.setDisable(false);
            }
        }

        //display register status
        statusLabel.setText("Available: " + event.getAvailableSeats() + " / " + event.getCapacity());

        //load image
        try {
            String imagePath = event.getHeaderImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imagePath = "images/default_header.png";
              }
            System.out.println(">>>imagePath: " + imagePath);
            if (imagePath == "default"){
                imagePath = "images/default_header.png";
            }
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                hearImageView.setImage(new Image(imageFile.toURI().toString()));
                currentImagePath = imagePath;
            } else {
                System.out.println("Image file not found: " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            try {
                // load default image
                String imagePath = "images/default_header.png";
                File imageFile = new File(imagePath);
                hearImageView.setImage(new Image(imageFile.toURI().toString()));
            } catch (Exception ex) {
                System.out.println("Error loading default image: " + ex.getMessage());
            }
        }
    }

    private Event createEventFromForm() {
        String eventName = eventNameField.getText();
        String eventCode = eventCodeField.getText();
        String description = descriptionArea.getText();
        String location = locationField.getText();

        LocalDate date = datePicker.getValue();
        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            time = LocalTime.now();
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);

        int capacity = Integer.parseInt(capacityField.getText());
        double cost = Double.parseDouble(costField.getText());

        Event event = new Event(eventName, eventCode, description, currentImagePath, location, dateTime, capacity, cost);
        return event;
    }

    private  void clearForm() {}

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
    }
}
