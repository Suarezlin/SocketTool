module com.linziniu {

    requires javafx.controls;
    requires javafx.fxml;
    requires com.linziniu.socket;
    requires org.apache.commons.lang3;
    requires org.jfxtras.styles.jmetro;

    opens com.linziniu to javafx.fxml, javafx.controls, javafx.graphics;
    opens com.linziniu.controller to javafx.fxml, javafx.controls, javafx.graphics;
    opens com.linziniu.model to javafx.fxml, javafx.controls;
}