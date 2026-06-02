package com.example.todolistapp;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;

public class ToDoListApp extends Application {

    // My data structure to hold user credentials for Authentication
    private HashMap<String, String> users;

    // I chose to store data in plain text (.txt) format instead of binary files
    private final String USERS_FILE = "users.txt";
    private String currentUser = null;

    // My task list and application scenes
    private ObservableList<String> tasks;
    private Stage window;
    private Scene loginScene, appScene;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("To-Do List Application - Final Project");

        // At startup, I load the registered users from the .txt file
        loadUsers();

        // I split the application flow into two parts: Login screen and Main app screen
        buildLoginScene();

        // To prevent data loss when the user clicks the close (X) button,
        // I write the current tasks to the text file before the window closes.
        window.setOnCloseRequest(e -> {
            if (currentUser != null) {
                saveTasks(currentUser);
            }
        });

        window.setScene(loginScene);
        window.show();
    }

    /// --- AUTHENTICATION (LOGIN/REGISTER) SCENE --- ///
    private void buildLoginScene() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Login or Register");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // My logic for registering a new user to the system
        registerButton.setOnAction(e -> {
            String user = usernameInput.getText();
            String pass = passwordInput.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                showAlert("Error", "Username or password cannot be empty!");
            } else if (users.containsKey(user)) {
                showAlert("Error", "This username is already registered!");
            } else {
                users.put(user, pass);
                saveUsers(); // I instantly write the new record to the .txt file
                showAlert("Success", "Registration successful! You can now log in.");
            }
        });

        // My logic for logging into the system
        loginButton.setOnAction(e -> {
            String user = usernameInput.getText();
            String pass = passwordInput.getText();

            // If the username and password match, I switch to the main application
            if (users.containsKey(user) && users.get(user).equals(pass)) {
                currentUser = user;
                buildAppScene();
                window.setScene(appScene);
            } else {
                showAlert("Error", "Invalid username or password!");
            }
        });

        HBox buttonBox = new HBox(10, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, usernameInput, passwordInput, buttonBox);
        loginScene = new Scene(layout, 400, 300);
    }

    /// --- MAIN APPLICATION (TO-DO LIST) SCENE --- ///
    private void buildAppScene() {
        tasks = FXCollections.observableArrayList();

        // I load the text file that belongs exclusively to the logged-in user
        loadTasks(currentUser);

        ListView<String> listView = new ListView<>(tasks);
        TextField taskInput = new TextField();
        taskInput.setPromptText("Enter a new task...");

        Button addButton = new Button("Add");
        Button completeButton = new Button("Complete");
        Button deleteButton = new Button("Delete");
        Button logoutButton = new Button("Logout");

        // Add Task Logic
        addButton.setOnAction(e -> {
            String newTask = taskInput.getText();
            if (newTask != null && !newTask.trim().isEmpty()) {
                tasks.add(newTask);
                taskInput.clear();
            }
        });

        // Mark as Completed Logic
        completeButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                String currentTask = tasks.get(selectedIndex);
                // If the task is not already marked, I add a checkmark at the beginning
                if (!currentTask.startsWith("[✓] ")) {
                    tasks.set(selectedIndex, "[✓] " + currentTask);
                }
            } else {
                showAlert("Warning", "Please select a task to mark as completed.");
            }
        });

        // Delete Task Logic
        deleteButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                tasks.remove(selectedIndex);
            } else {
                showAlert("Warning", "Please select a task to delete.");
            }
        });

        // Safe Logout Logic
        logoutButton.setOnAction(e -> {
            saveTasks(currentUser); // I save the current tasks to the text file before logging out
            currentUser = null;

            // I reset the login scene and navigate back to it
            buildLoginScene();
            window.setScene(loginScene);
        });

        HBox inputBox = new HBox(10, taskInput, addButton, completeButton, deleteButton);
        inputBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, new Label("Welcome, " + currentUser + "!"), listView, inputBox, logoutButton);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-font-size: 14px;");

        appScene = new Scene(root, 500, 450);
    }

    /// --- TEXT FILE I/O OPERATIONS --- ///

    // My method to read user credentials line by line from a simple .txt file
    private void loadUsers() {
        users = new HashMap<>();
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                // I read each line in "username,password" format and split it
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        users.put(parts[0], parts[1]);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }
    }

    // My method to write registered users into the .txt file
    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (String user : users.keySet()) {
                bw.write(user + "," + users.get(user));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // My method to read the tasks of the selected user from their specific .txt file
    private void loadTasks(String username) {
        File file = new File(username + "_tasks.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    tasks.add(line); // I add each read line to the list
                }
            } catch (IOException e) {
                System.err.println("Error loading tasks: " + e.getMessage());
            }
        }
    }

    // My method to write the updated tasks of the selected user to their .txt file
    private void saveTasks(String username) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(username + "_tasks.txt"))) {
            for (String task : tasks) {
                bw.write(task); // I write each item in the list line by line to the file
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    // My generic alert method created to prevent code duplication
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}