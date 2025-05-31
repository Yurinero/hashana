package me.yurinero.hashana.controllers;


import javafx.fxml.FXML;
import javafx.scene.Scene;
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
	public ChoiceBox<String> themeChoiceBox;


	private Stage stage;
	private Scene mainScene;
	private double xOffset = 0;
	private double yOffset = 0;

	public void initialize() {
	setupDragging();
	setupWindowControls();
	loadCurrentSettings();
	setupThemeSelector();
	}

	private void loadCurrentSettings() {
		UserSettings userSettings = UserSettings.getInstance();
		UserSettings.SettingsData currentSettings = userSettings.getSettings();
		bufferSize.setText(String.valueOf(currentSettings.bufferSize));
		maxFileSize.setText(String.valueOf(currentSettings.maxFileSize));
		progressIntervalMS.setText(String.valueOf(currentSettings.progressIntervalMS));
		splashScreenEnabled.setSelected(currentSettings.splashScreenEnabled);
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
		});
	}

	private String getCssPathForTheme(String themeName) {
		return switch (themeName) {
			case "Light" -> "/me/yurinero/hashana/light-theme.css";
			case "Accessible" -> "/me/yurinero/hashana/accessible-theme.css";
			case "Dark" -> "/me/yurinero/hashana/dark-theme.css"; // Default/fallback
			default -> {
				System.err.println("Unknown theme name: " + themeName + ". Defaulting to Dark.");
				yield "/me/yurinero/hashana/dark-theme.css";
			}
		};
	}

	private void applyThemeToScene(String themeName, Scene sceneToUpdate) {
		if (sceneToUpdate != null) return;
		String cssPath = getCssPathForTheme(themeName);
		try {
			String fullCssPath = getClass().getResource(cssPath).toExternalForm();
			sceneToUpdate.getStylesheets().clear();
			sceneToUpdate.getStylesheets().add(fullCssPath);
			System.out.println("Scene stylesheets NOW: " + sceneToUpdate.getStylesheets());
		} catch (NullPointerException e) {
			System.err.println("Could not find CSS file: " + cssPath);
		}
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
			newSettings.activeTheme = themeChoiceBox.getValue();

			userSettings.saveSettings();

			applyThemeToScene(newSettings.activeTheme, this.stage.getScene());
			if (mainScene !=null) {
				applyThemeToScene(newSettings.activeTheme, mainScene);
			}

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
			newSettings.activeTheme = themeChoiceBox.getValue();
			userSettings.saveSettings();

			applyThemeToScene(newSettings.activeTheme, this.stage.getScene());
			if (mainScene !=null) {
				applyThemeToScene(newSettings.activeTheme, mainScene);
			}

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
	public void setMainScene(Scene mainScene) {
		this.mainScene = mainScene;
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
