module com.linziniu {

    requires javafx.controls;
    requires javafx.fxml;
    requires com.linziniu.socket;

    opens com.linziniu to javafx.fxml, javafx.controls, javafx.graphics;
    opens com.linziniu.controller to javafx.fxml, javafx.controls, javafx.graphics;
}