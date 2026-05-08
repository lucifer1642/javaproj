package com.railpantry.screens;

import com.railpantry.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;

public class LoginScreen extends StackPane {

    public LoginScreen() {
        // ── Full-Screen Background (station image with dark overlay) ──────────
        StackPane bgLayer = new StackPane();

        // Try to load login_bg image
        InputStream bgStream = getClass().getResourceAsStream("/images/login_bg.png");
        if (bgStream != null) {
            ImageView bgImg = new ImageView(new Image(bgStream));
            bgImg.setPreserveRatio(false);
            bgImg.fitWidthProperty().bind(widthProperty());
            bgImg.fitHeightProperty().bind(heightProperty());

            // Dark overlay on top of the bg image
            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(10,20,50,0.72);");
            overlay.prefWidthProperty().bind(widthProperty());
            overlay.prefHeightProperty().bind(heightProperty());

            bgLayer.getChildren().addAll(bgImg, overlay);
        } else {
            // Fallback gradient
            setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");
        }

        // ── Login Card ────────────────────────────────────────────────────────
        VBox loginCard = new VBox(18);
        loginCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.97);" +
            "-fx-background-radius: 18px;" +
            "-fx-padding: 40px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 40, 0, 0, 12);"
        );
        loginCard.setMaxSize(420, 580);
        loginCard.setAlignment(Pos.CENTER);

        // ── IRCTC Logo + Brand Header ─────────────────────────────────────────
        InputStream logoStream = getClass().getResourceAsStream("/images/irctc.jpg");
        if (logoStream != null) {
            ImageView logoView = new ImageView(new Image(logoStream));
            logoView.setFitWidth(80);
            logoView.setFitHeight(80);
            logoView.setPreserveRatio(true);
            loginCard.getChildren().add(logoView);
        }

        // IRCTC badge row
        HBox irctcBadge = new HBox(8);
        irctcBadge.setAlignment(Pos.CENTER);
        Label irctcLbl = new Label("🇮🇳  IRCTC");
        irctcLbl.setStyle(
            "-fx-background-color: #1a365d; -fx-text-fill: white;" +
            "-fx-font-weight: 900; -fx-font-size: 11px; -fx-padding: 4px 12px;" +
            "-fx-background-radius: 20px; -fx-letter-spacing: 1px;"
        );
        Label certLbl = new Label("Indian Railways Certified");
        certLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        irctcBadge.getChildren().addAll(irctcLbl, certLbl);

        Label brand = new Label("RAILPANTRY");
        brand.setStyle(
            "-fx-text-fill: #1a365d; -fx-font-size: 28px;" +
            "-fx-font-weight: 900; -fx-letter-spacing: 3px;"
        );

        Label subBrand = new Label("🚄  Staff & Admin Portal");
        subBrand.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: bold;");

        VBox headerBox = new VBox(6, irctcBadge, brand, subBrand);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // ── Form Fields ───────────────────────────────────────────────────────
        VBox formBox = new VBox(14);

        VBox idBox = new VBox(5);
        Label idLabel = new Label("Employee ID");
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        TextField idField = new TextField();
        idField.setPromptText("e.g. RK-492 | type 'admin' for admin");
        idField.setStyle(
            "-fx-padding: 11px 14px; -fx-background-radius: 8px;" +
            "-fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-font-size: 13px;"
        );
        idBox.getChildren().addAll(idLabel, idField);

        VBox passBox = new VBox(5);
        Label passLabel = new Label("Secure Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password...");
        passField.setStyle(
            "-fx-padding: 11px 14px; -fx-background-radius: 8px;" +
            "-fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-font-size: 13px;"
        );
        passBox.getChildren().addAll(passLabel, passField);

        formBox.getChildren().addAll(idBox, passBox);

        // ── Login Button ──────────────────────────────────────────────────────
        Button loginBtn = new Button("🔐  Secure Login");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-padding: 13px; -fx-font-size: 14px;");

        // ── Error Label ───────────────────────────────────────────────────────
        Label errorLabel = new Label("⚠  Please fill in both fields to continue.");
        errorLabel.setStyle(
            "-fx-text-fill: #dc2626; -fx-font-size: 12px;" +
            "-fx-font-weight: bold; -fx-background-color: #fef2f2;" +
            "-fx-padding: 8px 14px; -fx-background-radius: 6px;"
        );
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        loginBtn.setOnAction(e -> {
            if (idField.getText().trim().isEmpty() || passField.getText().trim().isEmpty()) {
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                if (idField.getText().trim().equalsIgnoreCase("admin")) {
                    Main.navigateTo(new AdminDashboardScreen());
                } else {
                    Main.navigateTo(new ManagerDashboardScreen());
                }
            }
        });

        // ── Navigation Links ──────────────────────────────────────────────────
        HBox linkBox = new HBox(20);
        linkBox.setAlignment(Pos.CENTER);
        linkBox.setPadding(new Insets(6, 0, 0, 0));

        Label forgotPwdLbl = new Label("Forgot Password?");
        forgotPwdLbl.setStyle(
            "-fx-text-fill: #3b82f6; -fx-cursor: hand;" +
            "-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 12px;"
        );
        forgotPwdLbl.setOnMouseClicked(e -> Main.navigateTo(new ForgotPasswordScreen()));

        Label signUpLbl = new Label("Create Account");
        signUpLbl.setStyle(
            "-fx-text-fill: #3b82f6; -fx-cursor: hand;" +
            "-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 12px;"
        );
        signUpLbl.setOnMouseClicked(e -> Main.navigateTo(new RegisterScreen()));

        linkBox.getChildren().addAll(forgotPwdLbl, signUpLbl);

        // ── Footer Label ──────────────────────────────────────────────────────
        Label footer = new Label("🛡  Ministry of Railways | Indian Government");
        footer.setStyle(
            "-fx-text-fill: #94a3b8; -fx-font-size: 10px;" +
            "-fx-font-weight: bold; -fx-padding: 8px 0 0 0;"
        );

        loginCard.getChildren().addAll(headerBox, formBox, loginBtn, errorLabel, linkBox, footer);

        getChildren().addAll(bgLayer, loginCard);
    }
}
