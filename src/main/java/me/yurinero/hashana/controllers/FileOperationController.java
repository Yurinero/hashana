package me.yurinero.hashana.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.UserSettings;

import java.io.File;

public abstract class FileOperationController {

	protected File selectedFile;
	protected UserSettings.SettingsData appSettings;
	protected volatile boolean cancelRequested = false;
	protected long lastUpdateTime = 0;

	protected abstract TextField getFilePathField();
	protected abstract ProgressBar getProgressBar();
	protected abstract Label getProgressLabel();
	protected abstract Button getCancelButton();
	protected abstract AnchorPane getRootPane();

	protected abstract void onFileSelected(File file);

	protected abstract void onFileSelectionCancelled();

	@FXML
	public void initialize() {
		this.appSettings = UserSettings.getInstance().getSettings();
		getCancelButton().setDisable(true);
		resetProgress();
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		cancelRequested = true;
		getCancelButton().setDisable(true);
		getProgressLabel().setText("Cancelled -" + getProgressLabel().getText());
		getProgressBar().setStyle("-fx-accent: red;");
	}

	@FXML
	protected void handleFileBrowse(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a file");
		// Open file browser to choose file
		File tempSelectedFile = fileChooser.showOpenDialog(getFilePathField().getScene().getWindow());

		if (tempSelectedFile != null) {
			// Max acceptable file size bytes converted to MB
			long maxFileSizeBytes = appSettings.maxFileSize * 1024 * 1024;

			if (tempSelectedFile.length() > maxFileSizeBytes) {
				// If file is too large, show alert window
				String readableFileSize = String.format("%.2f MB", (double)maxFileSizeBytes / (1024 * 1024));
				String readableSelectedFileSize = String.format("%.2f MB", (double)tempSelectedFile.length() / (1024 * 1024));

				Alert alert = DialogUtils.createStyledAlert(
						Alert.AlertType.WARNING,
						"File Too Large",
						"The selected file exceeds the maximum allowed size.",
						"Selected file " + readableSelectedFileSize + "\nMaximum allowed: " + readableFileSize
				);

				alert.showAndWait();
				// Clear File Path and set file selection back to null
				getFilePathField().clear();
				this.selectedFile = null;
			} else {
				// If file size is acceptable, proceed
				this.selectedFile = tempSelectedFile;
				getFilePathField().setText(selectedFile.getAbsolutePath());
				onFileSelected(selectedFile);
			}
		}
	}

	protected void updateProgress(long bytesRead, long totalBytes) {
		Platform.runLater(() -> {
			double progress = (double) bytesRead / totalBytes;
			getProgressBar().setProgress(progress);
			getProgressLabel().setText(String.format("%s / %s (%.1f%%)",
					formatBytes(bytesRead),
					formatBytes(totalBytes),
					progress * 100
			));
		});
	}

	protected void resetProgress() {
		Platform.runLater(() -> {
			getProgressBar().setProgress(0);
			getProgressBar().setStyle("");
			getProgressLabel().setText("0%");
		});
	}

	protected String formatBytes(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp-1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	/** Generic method to add Accelerator's, effectively keyboard shortcuts.
	 *  Requires 3 values.
	 * @param keyCode  Key to be hit
	 * @param modifier  Modifier to be used, such as CTRL_DOWN
	 * @param action  Action to take upon completion
	 */

	public void addAccelerator(KeyCode keyCode, KeyCombination.Modifier modifier, Runnable action ) {
		KeyCombination keyCombination = new KeyCodeCombination(keyCode, modifier);
		getRootPane().sceneProperty().addListener((observable, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.getAccelerators().put(keyCombination, action);
			}
		});
	}

}
