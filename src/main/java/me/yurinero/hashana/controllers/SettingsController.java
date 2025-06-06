package me.yurinero.hashana.controllers;


import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.ThemeUtils;
import me.yurinero.hashana.utils.UserSettings;
import me.yurinero.hashana.utils.WindowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public ChoiceBox<String> themeChoiceBox;

	private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
	private final UserSettings userSettings = UserSettings.getInstance();
	private Stage stage;
	private Scene mainScene;

	public void initialize() {
	loadCurrentSettings();
	setupThemeSelector();
	}

	public void setStage(Stage stage) {
		this.stage = stage;

		WindowUtils.setupDragging(this.stage,settingsTitle);
		WindowUtils.setupCloseButton(this.stage,settingsClose);

	}
	public void setMainScene(Scene mainScene) {
		this.mainScene = mainScene;
	}

	private void loadCurrentSettings() {
		UserSettings.SettingsData currentSettings = userSettings.getSettings();
		bufferSize.setText(String.valueOf(currentSettings.bufferSize));
		maxFileSize.setText(String.valueOf(currentSettings.maxFileSize));
		progressIntervalMS.setText(String.valueOf(currentSettings.progressIntervalMS));
		splashScreenEnabled.setSelected(currentSettings.splashScreenEnabled);
		logger.info("Loaded settings from {}", currentSettings.toString());
	}

	private void setupThemeSelector() {
		themeChoiceBox.getItems().addAll("Light", "Dark", "Accessible");
		String currentTheme = UserSettings.getInstance().getSettings().activeTheme;
		themeChoiceBox.setValue(currentTheme);

		themeChoiceBox.valueProperty().addListener((observable, oldTheme, newTheme) -> {
			if (newTheme != null) {
				applyThemeToScene(newTheme, this.stage.getScene());
				if (mainScene !=null) {
					applyThemeToScene(newTheme, mainScene);
				}
			}
			UserSettings.getInstance().getSettings().activeTheme = newTheme;
			statusLabel.setText("Theme changed. Apply or Save to keep.");
			logger.info("Changed theme to {}", newTheme);
		});
	}


	private void applyThemeToScene(String themeName, Scene sceneToUpdate) {
		if (sceneToUpdate == null) return;
		String cssPath = ThemeUtils.getCssPathForTheme(themeName);
		try {
			String fullCssPath = getClass().getResource(cssPath).toExternalForm();
			sceneToUpdate.getStylesheets().clear();
			sceneToUpdate.getStylesheets().add(fullCssPath);
		} catch (NullPointerException e) {
			logger.error("Could not find CSS file:{}", cssPath, e);
		}
	}


	@FXML
	private void handleSettingsSave() {
		try {
			UserSettings.SettingsData newSettings = userSettings.getSettings();
			newSettings.bufferSize = Integer.parseInt(bufferSize.getText());
			newSettings.maxFileSize = Long.parseLong(maxFileSize.getText());
			newSettings.progressIntervalMS = Integer.parseInt(progressIntervalMS.getText());
			newSettings.splashScreenEnabled = splashScreenEnabled.isSelected();
			newSettings.activeTheme = themeChoiceBox.getValue();

			userSettings.saveSettings();
			logger.info("Saved settings to {}", userSettings.getSettings().toString());

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
			UserSettings.SettingsData newSettings = userSettings.getSettings();
			newSettings.bufferSize = Integer.parseInt(bufferSize.getText());
			newSettings.maxFileSize = Long.parseLong(maxFileSize.getText());
			newSettings.progressIntervalMS = Integer.parseInt(progressIntervalMS.getText());
			newSettings.splashScreenEnabled = splashScreenEnabled.isSelected();
			newSettings.activeTheme = themeChoiceBox.getValue();
			userSettings.saveSettings();
			logger.info("Applied & Saved new settings: {}", userSettings.getSettings().toString());

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

}
