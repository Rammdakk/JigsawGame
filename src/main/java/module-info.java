module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.hse.homework5 to javafx.fxml;
    exports com.hse.homework5;
}