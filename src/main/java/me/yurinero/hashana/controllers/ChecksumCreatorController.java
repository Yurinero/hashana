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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

public class ChecksumCreatorController extends FileOperationController {
	// UI Components
	public TextField filePathField;
	public Button browseButton;
	public ChoiceBox<String> algorithmChoiceBox;
	public Button createChecksumButton;
	public TextArea statusArea;
	public ProgressBar hashProgress;
	public Button cancelButton;
	public Label progressLabel;
	public AnchorPane rootAnchor;
	public TextArea helpTextArea;
	// Controller specific fields
	private  static final Logger logger = LoggerFactory.getLogger(ChecksumCreatorController.class);
	private final String[] supportedAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};
	private static final String HELP_TEXT_CONTENT =
			"""
			How to Use:
		
			1. Browse: Click 'Browse' or use Ctrl+O to select the file for which you want to create a checksum.
			2. Select Algorithm: Choose your desired hashing algorithm from the dropdown menu. SHA256 is selected by default.
			3. Create Checksum: Click the 'Create Checksum' button to begin.
			   - The progress bar will show the status of the hashing operation.
			   - You can cancel the process at any time using the 'Cancel' button or by pressing Ctrl+X.
			4. Auto-Save: Upon completion, a new checksum file (e.g., 'yourfile.ext.sha256') containing the calculated hash will be automatically saved in the same directory as your original file.
		
			Important Warning:
			This functionality uses hashing methods from Google Guava's library. Some of these methods might be labeled as experimental or beta by Guava. Please be mindful of this when using the file hashing features for critical applications.""";

	@FXML
	public void initialize() {
		// Initialize abstract base controller
		super.initialize();
		//Populate list with hashing algorithms from fileHashAlgorithms
		algorithmChoiceBox.getItems().addAll(supportedAlgorithms);
		//Add Accelerator aka Shortcut for File Browsing and Cancellation of ongoing File Hash Check
		addAccelerator(KeyCode.O, KeyCombination.CONTROL_DOWN,() -> browseButton.fire());
		addAccelerator(KeyCode.X, KeyCombination.CONTROL_DOWN,() -> getCancelButton().fire());
		//Set default SHA256 algorithm
		algorithmChoiceBox.setValue(supportedAlgorithms[0]);
		loadHelpText();
	}

	// Implementation of Abstract Methods
	@Override
	protected void onFileSelected(File file) {
		statusArea.setText("File Selected: " + file.getName());
	}

	@Override
	protected void onFileSelectionCancelled() {
		statusArea.setText("File selection cancelled or file was too large.");
	}

	@Override protected TextField getFilePathField() { return filePathField; }
	@Override protected ProgressBar getProgressBar() { return hashProgress; }
	@Override protected Label getProgressLabel() { return progressLabel; }
	@Override protected Button getCancelButton() { return cancelButton; }

	// Controller specific methods

	private void loadHelpText() {
		if (helpTextArea != null) {
			helpTextArea.setText(HELP_TEXT_CONTENT);
		}
	}

	@FXML
	private void handleCreateChecksum(ActionEvent event) {
		if (selectedFile == null) {
			statusArea.setText("Error: Please select a file first.");
			DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "No File Selected", "File Not Selected", "Please select a file before creating a checksum.").showAndWait();
			logger.error("Create checksum failed due to no file selected.");
			return;
		}

		String algorithm = algorithmChoiceBox.getValue();
		HashFunction hashFunction = HashUtils.getHashFunction(algorithm); //

		if (hashFunction == null) {
			statusArea.setText("Error: Invalid algorithm selected.");
			logger.error("Create checksum failed due to unsupported hash function selected.");
			return;
		}

		resetProgress();
		getCancelButton().setDisable(false);
		createChecksumButton.setDisable(true);
		browseButton.setDisable(true);
		cancelRequested = false;

		statusArea.setText("Calculating " + algorithm + " hash for " + selectedFile.getName() + "...");

		ExecutorService executor = ThreadPoolService.getInstance().getExecutorService();
		executor.submit(() -> createHashInBackground(hashFunction,algorithm));
		logger.debug("Attempting hash calculation for file:  {}.", selectedFile.getName());
	}

	private void createHashInBackground(HashFunction hashFunction, String algorithm) {
		try (InputStream is = new FileInputStream(selectedFile)) {
			HashCode hashCode = hashStream(is, hashFunction);

			if (hashCode != null) {
				Platform.runLater(() -> {
					String hashString = hashCode.toString();
					statusArea.appendText("\nHash calculated: " + hashString);
					saveChecksumFile(hashString, algorithm);
					logger.debug("Hash calculated: {}", hashString);
				});

			} else {
				Platform.runLater(() -> statusArea.appendText("\nHash calculation cancelled by user."));
				logger.info("Hash calculation cancelled by user.");
			}

		} catch (IOException e) {
			Platform.runLater(() -> {
				statusArea.appendText("\nError reading file: " + e.getMessage());
				DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "File Error", "Error Reading File", "Could not read the selected file: " + e.getMessage()).showAndWait();
				logger.error("Error reading file: {}", e.getMessage());
				resetUIState();
			});
		} finally {
			Platform.runLater(this::resetUIState);
		}
	}

	private void saveChecksumFile(String hashString, String algorithm) {
		Path originalFilePath = selectedFile.toPath();
		String checksumFileName = selectedFile.getName() + "." + algorithm.toLowerCase();
		Path checksumFilePath = originalFilePath.resolveSibling(checksumFileName);

		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date());
		String content = String.format("%s  %s\n# Algorithm: %s\n# Created: %s\n# Generated using: Hashana",
				hashString, selectedFile.getName(), algorithm, timestamp);


		try {
			Files.writeString(checksumFilePath, content);
			statusArea.appendText("\nChecksum file saved: " + checksumFilePath);
			DialogUtils.createStyledAlert(Alert.AlertType.INFORMATION, "Success", "Checksum File Created", "Checksum file saved successfully at:\n" + checksumFilePath).showAndWait();
			logger.debug("Checksum file saved: {}", checksumFilePath);
		} catch (IOException e) {
			statusArea.appendText("\nError saving checksum file: " + e.getMessage());
			DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "Save Error", "Error Saving Checksum File", "Could not save the checksum file: " + e.getMessage()).showAndWait();
			logger.debug("Error saving checksum file: {}", e.getMessage());
		}
	}


	private void resetUIState() {
		getCancelButton().setDisable(true);
		createChecksumButton.setDisable(false);
		browseButton.setDisable(false);
		logger.info("Reset UI state.");
	}

}
