package com.railpantry.screens;

import com.railpantry.Main;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegisterScreen extends StackPane {

    public RegisterScreen() {
        setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        VBox card = new VBox(15);
        card.getStyleClass().add("summary-card");
        card.setMaxSize(450, 600);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setAlignment(Pos.CENTER);
        
        Label brand = new Label("RAILPANTRY");
        brand.setStyle("-fx-text-fill: #1a365d; -fx-font-size: 22px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        Label subBrand = new Label("Request System Access");
        subBrand.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        VBox headerBox = new VBox(5, brand, subBrand);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        // Form Fields
        VBox formBox = new VBox(12);
        
        TextField nameField = createStyledTextField("Full Legal Name");
        TextField idField = createStyledTextField("Requested Employee ID");
        
        // Roles ComboBox
        VBox roleBox = new VBox(5);
        Label roleLabel = new Label("Requested Security Role");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("Pantry Staff", "Pantry Manager", "Route Administrator", "System Admin"));
        roleCombo.getSelectionModel().selectFirst();
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle("-fx-padding: 5px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #ffffff;");
        roleBox.getChildren().addAll(roleLabel, roleCombo);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Create Secure Password");
        passField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        PasswordField confField = new PasswordField();
        confField.setPromptText("Confirm Password");
        confField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        formBox.getChildren().addAll(
            new VBox(5, createStrongLabel("Full Legal Name"), nameField),
            new VBox(5, createStrongLabel("Employee ID"), idField),
            roleBox,
            new VBox(5, createStrongLabel("Password"), passField),
            new VBox(5, createStrongLabel("Confirm Password"), confField)
        );

        Button registerBtn = new Button("Submit Request");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-padding: 12px; -fx-font-size: 14px; -fx-margin-top: 10px;");

        Label backLbl = new Label("\u2190 Back to Login");
        backLbl.setStyle("-fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 15px 0 0 0;");
        backLbl.setOnMouseClicked(e -> Main.navigateTo(new LoginScreen()));

        registerBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account Request Submitted");
            alert.setHeaderText("Pending Administrative Approval");
            alert.setContentText("Your request for the role [" + roleCombo.getValue() + "] has been forwarded to the IRCTC Admin dashboard. You will be able to log in once approved.");
            alert.showAndWait();
            Main.navigateTo(new LoginScreen());
        });

        card.getChildren().addAll(headerBox, formBox, registerBtn, backLbl);
        getChildren().add(card);
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        return tf;
    }

    private Label createStrongLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        return lbl;
    }
}
