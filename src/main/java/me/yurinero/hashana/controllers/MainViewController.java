/*
 * Hashana - A desktop utility for hashing and password generation.
 * Copyright (C) 2025 Yurinero <https://github.com/Yurinero>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.yurinero.hashana.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.yurinero.hashana.utils.ThemeUtils;
import me.yurinero.hashana.utils.UserSettings;
import me.yurinero.hashana.utils.WindowResizer;
import me.yurinero.hashana.utils.WindowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/* This is the controller that allows us to achieve a more customized look, so we can remove the system default title bar
*  and replace it with our own, while still allowing the user to minimize/close and window drag. It can further be improved
*  by adding controls to resize the window, but as the app itself is very small and displays little content, I do not see the point as of the moment.
*/

public class MainViewController implements Initializable {

	public HBox titleBar;
	public Button settingsButton;
	public Button minimizeButton;
	public Button closeButton;
	public Button maximizeButton;

	private Stage stage;
	private  static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	settingsButton.setOnAction(this::openSettings);
	}


	public void setStage(Stage stage) {
		this.stage = stage;

		// Setup window controls using WindowUtils class.
		WindowUtils.setupDragging(this.stage, titleBar);
		WindowUtils.setupMinimizeButton(this.stage, minimizeButton);
		WindowUtils.setupMaximizeRestore(this.stage, maximizeButton);
		new WindowResizer(stage);

		// Since the main window also exists the application, it needs specific logic for it, as such the WindowUtils class isn't relevant.
		closeButton.setOnAction(event -> {
			Platform.exit();
			System.exit(0);
		});
		// Dynamically change the font size based on window size.
		// Add a listener to the stage's width property
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> {
			// Calculate a new base font size.
			double newSize = newVal.doubleValue() / 90; // Example formula

			// Set a minimum size to prevent text from becoming too small
			if (newSize < 12) {
				newSize = 12;
			}

			// Get the root node of the scene and update its font size style
			Parent root = this.stage.getScene().getRoot();
			root.setStyle("-fx-font-size: " + newSize + "px;");
		});
		logger.debug("Setting up window controls");
	}

	@FXML
	private void openSettings(ActionEvent event) {
		try {
			logger.debug("Opening settings window");
			// Load the settings FXML file
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/yurinero/hashana/view/settings-view.fxml"));
			Parent root = loader.load();

			// Create new stage for settings window
			Stage settingsStage = new Stage();
			Scene settingsScene = new Scene(root);
			// Load the relevant controller
			SettingsController controller = loader.getController();
			// Pass the stage to the controller
			controller.setStage(settingsStage);
			if (this.stage != null) { // 'this.stage' is the main application stage
				controller.setMainScene(this.stage.getScene());
			}
			settingsStage.setTitle("Settings");
			settingsStage.setScene(settingsScene);

			String currentTheme = UserSettings.getInstance().getSettings().activeTheme;
			String cssPath = ThemeUtils.getCssPathForTheme(currentTheme);
			try {
				String fullCssPath = getClass().getResource(cssPath).toExternalForm();
				settingsScene.getStylesheets().add(fullCssPath);
			} catch (NullPointerException e) {
				logger.error("Error: Could not find CSS file for settings window: {}", cssPath);
			}

			// Set modality to block main window interaction
			settingsStage.initModality(Modality.APPLICATION_MODAL);
			// Disable system default window styling
			settingsStage.initStyle(StageStyle.UNDECORATED);
			// Set owner to link windows
			Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			settingsStage.initOwner(mainStage);
			// Show the result
			settingsStage.show();
		} catch (IOException e) {
			logger.error("Error: Could not open settings window: {}", e.getMessage());

		}
	}
}
