package me.yurinero.hashana.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.UserSettings;

public class SettingsController {
	public TextField bufferSize;
	public TextField maxFileSize;
	public TextField progressIntervalMS;
	public CheckBox splashScreenEnabled;
	public HBox settingsTitle;
	public Button settingsApply;
	public Button settingsSave;
	public Button settingsClose;
	public Label statusLabel;

	private Stage stage;
	private double xOffset = 0;
	private double yOffset = 0;

	public void initialize() {
	setupDragging();
	setupWindowControls();
	UserSettings userSettings = UserSettings.getInstance();
	UserSettings.SettingsData currentSettings = userSettings.getSettings();
	bufferSize.setText(String.valueOf(currentSettings.bufferSize));
	maxFileSize.setText(String.valueOf(currentSettings.maxFileSize));
	progressIntervalMS.setText(String.valueOf(currentSettings.progressIntervalMS));
	splashScreenEnabled.setSelected(currentSettings.splashScreenEnabled);
	}

	@FXML
	private void handleSettingsSave() {
		try {
			UserSettings userSettings = UserSettings.getInstance();
			UserSettings.SettingsData newSettings = userSettings.getSettings();
			newSettings.bufferSize = Integer.parseInt(bufferSize.getText());
			newSettings.maxFileSize = Long.parseLong(maxFileSize.getText());
			newSettings.progressIntervalMS = Integer.parseInt(progressIntervalMS.getText());
			newSettings.splashScreenEnabled = splashScreenEnabled.isSelected();

			userSettings.saveSettings();

			stage.close();
		} catch (NumberFormatException e) {
			Alert alert = DialogUtils.createStyledAlert(
					Alert.AlertType.ERROR,
					"Invalid Input",
					"Error in Settings Value",
					"Please ensure all numeric fields (Buffer Size, Max File Size, Progress Interval) contain valid numbers.");
			alert.showAndWait();
		}
	}

	@FXML
	private void handleSettingsApply() {
		try {
			UserSettings userSettings = UserSettings.getInstance();
			UserSettings.SettingsData newSettings = userSettings.getSettings();
			newSettings.bufferSize = Integer.parseInt(bufferSize.getText());
			newSettings.maxFileSize = Long.parseLong(maxFileSize.getText());
			newSettings.progressIntervalMS = Integer.parseInt(progressIntervalMS.getText());
			newSettings.splashScreenEnabled = splashScreenEnabled.isSelected();

			userSettings.saveSettings();
			statusLabel.setText("Settings applied!");
		} catch (NumberFormatException e) {
			Alert alert = DialogUtils.createStyledAlert(
					Alert.AlertType.ERROR,
					"Invalid Input",
					"Error in Settings Value",
					"Please ensure all numeric fields (Buffer Size, Max File Size, Progress Interval) contain valid numbers.");
			alert.showAndWait();
		}
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
