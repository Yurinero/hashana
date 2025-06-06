package me.yurinero.hashana.controllers;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
	// Controller specific fields
	private final String[] supportedAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};

	@FXML
	public void initialize() {
		// Initialize abstract base controller
		super.initialize();
		//Populate list with hashing algorithms from fileHashAlgorithms
		algorithmChoiceBox.getItems().addAll(supportedAlgorithms);
		//Set default SHA256 algorithm
		algorithmChoiceBox.setValue(supportedAlgorithms[0]);
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
	@Override protected AnchorPane getRootPane() { return (AnchorPane) filePathField.getParent(); }

	// Controller specific methods

	@FXML
	private void handleCreateChecksum(ActionEvent event) {
		if (selectedFile == null) {
			statusArea.setText("Error: Please select a file first.");
			DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "No File Selected", "File Not Selected", "Please select a file before creating a checksum.").showAndWait();
			return;
		}

		String algorithm = algorithmChoiceBox.getValue();
		HashFunction hashFunction = HashUtils.getHashFunction(algorithm); //

		if (hashFunction == null) {
			statusArea.setText("Error: Invalid algorithm selected.");
			return;
		}

		resetProgress();
		getCancelButton().setDisable(false);
		createChecksumButton.setDisable(true);
		browseButton.setDisable(true);
		cancelRequested = false;

		statusArea.setText("Calculating " + algorithm + " hash for " + selectedFile.getName() + "...");

		ExecutorService executor = ThreadPoolService.getInstance().getExecutorService(); //
		executor.submit(() -> {
			try (InputStream is = new FileInputStream(selectedFile)) {
				long fileSize = selectedFile.length();
				Hasher hasher = hashFunction.newHasher();
				byte[] buffer = new byte[appSettings.bufferSize * 1024]; // Use buffer size from settings
				long bytesRead = 0;
				int readCount;

				updateProgress(0, fileSize);

				while ((readCount = is.read(buffer)) != -1 && !cancelRequested) {
					hasher.putBytes(buffer, 0, readCount);
					bytesRead += readCount;
					long now = System.currentTimeMillis();
					if (now - lastUpdateTime > appSettings.progressIntervalMS || bytesRead == fileSize) { // Use progress interval from settings
						updateProgress(bytesRead, fileSize);
						lastUpdateTime = now;
					}
				}

				if (cancelRequested) {
					Platform.runLater(() -> {
						statusArea.appendText("\nHash calculation cancelled by user.");
						resetUIState();
					});
					return;
				}

				HashCode hashCode = hasher.hash();
				String hashString = hashCode.toString();

				Platform.runLater(() -> {
					statusArea.appendText("\nHash calculated: " + hashString);
					saveChecksumFile(hashString, algorithm);
					resetUIState();
				});

			} catch (IOException e) {
				Platform.runLater(() -> {
					statusArea.appendText("\nError reading file: " + e.getMessage());
					DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "File Error", "Error Reading File", "Could not read the selected file: " + e.getMessage()).showAndWait();
					resetUIState();
				});
			} finally {
				Platform.runLater(this::resetUIState);
			}
		});
	}

	private void saveChecksumFile(String hashString, String algorithm) {
		Path originalFilePath = selectedFile.toPath();
		String checksumFileName = selectedFile.getName() + "." + algorithm.toLowerCase();
		Path checksumFilePath = originalFilePath.resolveSibling(checksumFileName);

		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date());
		String content = String.format("%s  %s\n# Algorithm: %s\n# Created: %s\n# Generated using: Hashana",
				hashString, selectedFile.getName(), algorithm, timestamp);


		try {
			Files.write(checksumFilePath, content.getBytes(StandardCharsets.UTF_8));
			statusArea.appendText("\nChecksum file saved: " + checksumFilePath.toString());
			DialogUtils.createStyledAlert(Alert.AlertType.INFORMATION, "Success", "Checksum File Created", "Checksum file saved successfully at:\n" + checksumFilePath).showAndWait();
		} catch (IOException e) {
			statusArea.appendText("\nError saving checksum file: " + e.getMessage());
			DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "Save Error", "Error Saving Checksum File", "Could not save the checksum file: " + e.getMessage()).showAndWait();
		}
	}


	private void resetUIState() {
		getCancelButton().setDisable(true);
		createChecksumButton.setDisable(false);
		browseButton.setDisable(false);
	}

}
