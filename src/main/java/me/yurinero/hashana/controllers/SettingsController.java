package me.yurinero.hashana.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SettingsController {
	public TextField bufferSize;
	public TextField maxFileSize;
	public TextField progressIntervalMS;
	public CheckBox splashScreenEnabled;
	public HBox settingsTitle;
	public Button settingsApply;
	public Button settingsSave;
	public Button settingsClose;

	private Stage stage;
	private double xOffset = 0;
	private double yOffset = 0;

	public void initialize() {
	setupDragging();
	setupWindowControls();
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	private void setupWindowControls(){
		// Close button action
		settingsClose.setOnAction(event -> {
			stage.close();
		});

	}

	// Re-using code, might move into util class at some point.
	private void setupDragging() {
		// Capture initial mouse position relative to the stage
		settingsTitle.setOnMousePressed(event -> {

			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		// Move the stage using the initial offsets
		settingsTitle.setOnMouseDragged(event -> {

			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});
	}
}
