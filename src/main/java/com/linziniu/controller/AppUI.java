package com.linziniu.controller;

import com.linziniu.model.AppModel;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AppUI implements Initializable {

    public Label text;
    private AppModel model = new AppModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        text.textProperty().bindBidirectional(model.textProperty());
        model.setText("Hello JavaFX.");
    }

    public void test() {

    }

}
