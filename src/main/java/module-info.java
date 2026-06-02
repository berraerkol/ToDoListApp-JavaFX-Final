module com.example.todolistapp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.todolistapp to javafx.graphics, javafx.fxml;
    exports com.example.todolistapp;
}