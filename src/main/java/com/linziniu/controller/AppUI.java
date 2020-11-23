package com.linziniu.controller;

import com.linziniu.model.AppModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
//import jfxtras.styles.jmetro.JMetroStyleClass;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppUI implements Initializable {

    @FXML
    public TabPane tabPane;

    @FXML
    public MenuBar menu;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        tabPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        menu.setUseSystemMenuBar(true);
        System.out.println(menu.isUseSystemMenuBar());
    }

    public void test() {

    }

}
