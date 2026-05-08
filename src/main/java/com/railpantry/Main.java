package com.railpantry;

import com.railpantry.screens.AdminDashboardScreen;
import com.railpantry.screens.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene primaryScene;

    @Override
    public void start(Stage primaryStage) {
        LoginScreen loginScreen = new LoginScreen();
        
        primaryScene = new Scene(loginScreen, 1200, 850);
        String css = getClass().getResource("/css/style.css").toExternalForm();
        primaryScene.getStylesheets().add(css);
        
        primaryStage.setTitle("RailPantry - Pantry Management System");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    public static void navigateTo(Pane newRoot) {
        primaryScene.setRoot(newRoot);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
