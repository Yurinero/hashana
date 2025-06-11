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

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract base controller for views that perform hashing operations on files.
 * It handles common UI logic for file Browse, size validation, and progress updates.
 */
public abstract class FileOperationController {
	private  static final Logger logger = LoggerFactory.getLogger(FileOperationController.class);
	// Shared state
	protected File selectedFile;
	protected UserSettings.SettingsData appSettings;
	protected volatile boolean cancelRequested = false;
	protected long lastUpdateTime = 0;

	// Abstract methods for Subclass UI components
	protected abstract TextField getFilePathField();
	protected abstract ProgressBar getProgressBar();
	protected abstract Label getProgressLabel();
	protected abstract Button getCancelButton();
	protected abstract AnchorPane getRootPane();

	// Abstract hooks for subclass specific logic

	/**
	 * Called after a file has been successfully selected and validated.
	 * @param file The selected file.
	 */
	protected abstract void onFileSelected(File file);

	/**
	 * Called when the file selection is cleared or fails validation.
	 */
	protected abstract void onFileSelectionCancelled();

	/**
	 * Base initialization logic. Subclasses should call this from their own initialize methods using super.initialize()
	 */
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

	/**
	 * Handles the "Browse" button click. Shows a FileChooser, validates the
	 * selected file's size, and then calls the appropriate hook.
	 */
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
				logger.error("File: {} Size Too Large: {}",readableSelectedFileSize, readableFileSize);
				alert.showAndWait();
				// Clear File Path and set file selection back to null
				getFilePathField().clear();
				this.selectedFile = null;
			} else {
				// If file size is acceptable, proceed
				this.selectedFile = tempSelectedFile;
				getFilePathField().setText(selectedFile.getAbsolutePath());
				onFileSelected(selectedFile);
				logger.debug("File size OK, File selected: {}", selectedFile.getAbsolutePath());
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

	/** Reads an InputStream, computes its hash using the provided HashFunction, and updates the UI progress along the way.
	 *
	 * @param stream The InputStream of the file to hash.
	 * @param hashFunction The Guava hash function to use.
	 * @return The resulting HashCode, or null if the operation was cancelled.
	 * @throws IOException if there is an error reading the stream
	 */
	protected HashCode hashStream (InputStream stream, HashFunction hashFunction) throws IOException {
		long fileSize = selectedFile.length();
		Hasher hasher = hashFunction.newHasher();
		byte[] buffer = new byte[appSettings.bufferSize * 1024];
		long bytesRead = 0;
		int read;

		updateProgress(0, fileSize);

		while ((read = stream.read(buffer)) != -1 && !cancelRequested) {
			hasher.putBytes(buffer, 0, read);
			bytesRead += read;

			long now = System.currentTimeMillis();
			if (now - lastUpdateTime > appSettings.progressIntervalMS || bytesRead == fileSize) {
				updateProgress(bytesRead, fileSize);
				lastUpdateTime = now;
			}
		}
		if (cancelRequested) {
			return null;
		}
		return hasher.hash();
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
