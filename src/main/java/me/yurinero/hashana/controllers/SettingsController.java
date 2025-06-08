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

import java.util.ArrayList;
import java.util.List;

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
	public TextField maxEntropyPool;

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
		maxEntropyPool.setText(String.valueOf(currentSettings.maxEntropyLength));
		logger.info("Loaded settings from {}", currentSettings);
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

	/**
	 * A helper method to display a styled error alert with a given message.
	 * @param content The description of the error(s), gathered in a single pass and added with String.join()
	 */
	private void showErrorAlert(String content) {
		Alert alert = DialogUtils.createStyledAlert(
				Alert.AlertType.ERROR,
				"Invalid Input",
				"Error in Settings Value",
				content);
		alert.showAndWait();
	}

	/**
	 * Validates all numeric settings fields and saves them if they are valid.
	 * @return true if settings were successfully validated and saved, false otherwise.
	 */
	private boolean validateAndSaveSettings() {
		List<String> errors = new ArrayList<>();
		try {
			int newBufferSize = Integer.parseInt(bufferSize.getText());
			long newMaxFileSize = Long.parseLong(maxFileSize.getText());
			int newProgressInterval = Integer.parseInt(progressIntervalMS.getText());
			int newMaxEntropy = Integer.parseInt(maxEntropyPool.getText());

			//  Validation Pass
			if (newBufferSize <= 0) {
				errors.add("• Buffer Size must be a positive number.");
			}
			if (newMaxFileSize <= 0) {
				errors.add("• Max File Size must be a positive number.");
			}
			if (newProgressInterval <= 0) {
				errors.add("• Progress Interval must be a positive number.");
			}
			if (newMaxEntropy < 128) {
				errors.add("• Max Entropy Length must be at least 128 for security.");
			}

			// --- Error Reporting ---
			if (!errors.isEmpty()) {
				showErrorAlert(String.join("\n", errors));
				// Validation failed
				return false;
			}

			//  Save upon successful validation ---
			UserSettings.SettingsData settings = userSettings.getSettings();
			settings.bufferSize = newBufferSize;
			settings.maxFileSize = newMaxFileSize;
			settings.progressIntervalMS = newProgressInterval;
			settings.maxEntropyLength = newMaxEntropy;
			settings.splashScreenEnabled = splashScreenEnabled.isSelected();
			settings.activeTheme = themeChoiceBox.getValue();
			userSettings.saveSettings();
			// Success
			logger.info("Applied & Saved new settings: {}", settings.toString());
			return true;
			// Validation failed
		} catch (NumberFormatException e) {
			showErrorAlert("All numeric fields must contain valid numbers.");
			return false;
		}
	}

	@FXML
	private void handleSettingsSave() {
		if (validateAndSaveSettings()) {
			stage.close();
		}
	}

	@FXML
	private void handleSettingsApply() {
		if (validateAndSaveSettings()) {
			statusLabel.setText("Settings applied!");
		}
	}

}
