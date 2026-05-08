package com.railpantry.screens;

import com.railpantry.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ForgotPasswordScreen extends StackPane {

    public ForgotPasswordScreen() {
        setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        VBox card = new VBox(20);
        card.getStyleClass().add("summary-card");
        card.setMaxSize(400, 350);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        
        Label brand = new Label("RAILPANTRY");
        brand.setStyle("-fx-text-fill: #1a365d; -fx-font-size: 22px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        Label subBrand = new Label("Password Recovery");
        subBrand.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        VBox headerBox = new VBox(5, brand, subBrand);
        headerBox.setAlignment(Pos.CENTER);

        Label instructions = new Label("Enter your Employee ID. A recovery code will be dispatched to your registered operational device.");
        instructions.setWrapText(true);
        instructions.setStyle("-fx-text-fill: #64748b; -fx-text-alignment: center;");

        VBox idBox = new VBox(5);
        Label idLabel = new Label("Employee ID");
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField idField = new TextField();
        idField.setPromptText("e.g. RK-492");
        idField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        idBox.getChildren().addAll(idLabel, idField);

        Button requestBtn = new Button("Dispatch Code");
        requestBtn.getStyleClass().add("btn-primary");
        requestBtn.setMaxWidth(Double.MAX_VALUE);
        requestBtn.setStyle("-fx-padding: 12px; -fx-font-size: 14px;");

        Label backLbl = new Label("\u2190 Back to Login");
        backLbl.setStyle("-fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold;");
        backLbl.setOnMouseClicked(e -> Main.navigateTo(new LoginScreen()));

        requestBtn.setOnAction(e -> {
            if(!idField.getText().isEmpty()) {
                // Simulate success and move to Reset view
                Main.navigateTo(new ResetPasswordScreen());
            }
        });

        card.getChildren().addAll(headerBox, instructions, idBox, requestBtn, backLbl);
        getChildren().add(card);
    }
}
