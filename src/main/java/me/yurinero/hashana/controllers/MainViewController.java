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

	private Stage stage;
	private double xOffset = 0;
	private double yOffset = 0;
	// private boolean maximized = false;
	// private double originalWidth, originalHeight, originalX, originalY;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	setupWindowControls();
	setupDragging();
	settingsButton.setOnAction(this::openSettings);
	}



	public void setStage(Stage stage) {
		this.stage = stage;
	}

	private void setupWindowControls(){
		// Minimize button action
		minimizeButton.setOnAction(event -> stage.setIconified(true));

		// Close button action
		closeButton.setOnAction(event -> {
			Platform.exit();
			System.exit(0);
		});

	}

	private void setupDragging(){
		// Capture initial mouse position relative to the stage
		titleBar.setOnMousePressed(event -> {

			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		// Move the stage using the initial offsets
		titleBar.setOnMouseDragged(event -> {

			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});
	}
	@FXML
	private void openSettings(ActionEvent event) {
		try {
			// Load the settings FXML file
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/yurinero/hashana/settings-view.fxml"));
			Parent root = loader.load();

			// Create new stage for settings window
			Stage settingsStage = new Stage();
			// Load the relevant controller
			SettingsController controller = loader.getController();
			// Pass the stage to the controller
			controller.setStage(settingsStage);
			settingsStage.setTitle("Settings");
			settingsStage.setScene(new Scene(root));

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
			System.err.println("Error loading settings window:");
			e.printStackTrace();

		}
	}

	// Setup for window maximization, currently unused  but here if needed to be implemented.
	/*
	private void maximizeWindow(){
		if (stage == null) return;

		originalWidth = stage.getWidth();
		originalHeight = stage.getHeight();
		originalX = stage.getX();
		originalY = stage.getY();

		stage.setX(0);
		stage.setY(0);
		stage.setWidth(stage.getOwner() == null ? 0 : stage.getOwner().getWidth());
		stage.setHeight(stage.getOwner() == null ? 0 : stage.getOwner().getHeight());

		maximized = true;

	}


	private void restoreWindow(){
		if (stage == null) return;

		stage.setX(originalX);
		stage.setY(originalY);
		stage.setWidth(originalWidth);
		stage.setHeight(originalHeight);

		maximized = false;
	}
	*/

}
