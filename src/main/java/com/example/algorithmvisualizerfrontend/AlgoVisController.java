package com.example.algorithmvisualizerfrontend;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AlgoVisController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Algorith Visualizer.");
    }
}