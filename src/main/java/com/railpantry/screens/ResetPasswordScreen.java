package com.railpantry.screens;

import com.railpantry.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ResetPasswordScreen extends StackPane {

    public ResetPasswordScreen() {
        setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        VBox card = new VBox(15);
        card.getStyleClass().add("summary-card");
        card.setMaxSize(400, 480);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setAlignment(Pos.CENTER);
        
        Label brand = new Label("RAILPANTRY");
        brand.setStyle("-fx-text-fill: #1a365d; -fx-font-size: 22px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        Label subBrand = new Label("Finalize New Password");
        subBrand.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        VBox headerBox = new VBox(5, brand, subBrand);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        VBox formBox = new VBox(12);

        VBox codeBox = new VBox(5);
        Label codeLabel = new Label("Recovery Code");
        codeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField codeField = new TextField();
        codeField.setPromptText("Enter 6-digit code");
        codeField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        codeBox.getChildren().addAll(codeLabel, codeField);

        VBox passBox = new VBox(5);
        Label passLabel = new Label("New Secure Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("At least 8 characters");
        passField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        passBox.getChildren().addAll(passLabel, passField);
        
        VBox confBox = new VBox(5);
        Label confLabel = new Label("Confirm Password");
        confLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField confField = new PasswordField();
        confField.setPromptText("Match password above");
        confField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        confBox.getChildren().addAll(confLabel, confField);

        formBox.getChildren().addAll(codeBox, passBox, confBox);

        Button submitBtn = new Button("Confirm Password Reset");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-padding: 12px; -fx-font-size: 14px; -fx-margin-top: 10px;");

        Label backLbl = new Label("\u2190 Back to Login");
        backLbl.setStyle("-fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10px 0 0 0;");
        backLbl.setOnMouseClicked(e -> Main.navigateTo(new LoginScreen()));

        submitBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Password Updated");
            alert.setContentText("Your system credentials have been updated. You may now sign in.");
            alert.showAndWait();
            Main.navigateTo(new LoginScreen());
        });

        card.getChildren().addAll(headerBox, formBox, submitBtn, backLbl);
        getChildren().add(card);
    }
}
