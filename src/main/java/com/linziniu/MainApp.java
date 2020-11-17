package com.linziniu;

import com.linziniu.controller.AppUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jfxtras.resources.JFXtrasFontRoboto;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Socket 测试工具");
        Font font = Font.loadFont(getClass().getResource("/fonts/YaHei.ttf").toExternalForm(), 14);
        System.out.println(font);
        Font.loadFont(getClass().getResource("/fonts/PingFang-SC-Regular.ttf").toExternalForm(), 14);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/AppUI.fxml"));
        JFXtrasFontRoboto.loadAll();
        Scene scene = new Scene(root);
        for (String fontFamily : Font.getFamilies()) {
            System.out.println(fontFamily);
        }
        JMetro jmetro = new JMetro(Style.LIGHT);
//        scene.getStylesheets().add(getClass().getResource("/css/font.css").toExternalForm());
        jmetro.setScene(scene);
        jmetro.getOverridingStylesheets().addAll(getClass().getResource("/css/font.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
