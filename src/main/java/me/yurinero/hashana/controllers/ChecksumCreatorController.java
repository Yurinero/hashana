package me.yurinero.hashana.controllers;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import me.yurinero.hashana.utils.DialogUtils;
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import me.yurinero.hashana.utils.UserSettings;

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

public class ChecksumCreatorController {

	public TextField filePathField;
	public Button browseButton;
	public ChoiceBox<String> algorithmChoiceBox;
	public Button createChecksumButton;
	public TextArea statusArea;
	public ProgressBar hashProgress;
	public Button cancelButton;
	public Label progressLabel;

	private final String[] supportedAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};
	private volatile boolean cancelRequested = false;
	private long lastUpdateTime = 0;
	private UserSettings.SettingsData appSettings;
	private File selectedFile;

	@FXML
	private void initialize() {
		algorithmChoiceBox.getItems().addAll(supportedAlgorithms);
		algorithmChoiceBox.setValue(supportedAlgorithms[0]);
		cancelButton.setDisable(true);
		appSettings = UserSettings.getInstance().getSettings();
		resetProgress();
	}
	@FXML
	private void handleFileBrowse(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File to Create Checksum");
		// Open file browser to choose file
		File tempSelectedFile = fileChooser.showOpenDialog(filePathField.getScene().getWindow());

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
				filePathField.clear();
				this.selectedFile = null;
			} else {
				// If file size is acceptable, proceed
				this.selectedFile = tempSelectedFile;
				filePathField.setText(selectedFile.getAbsolutePath());
				statusArea.setText("File Selected: " + selectedFile.getName());
			}
		}
	}

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
		cancelButton.setDisable(false);
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
		String content = String.format("%s *%s\n# Algorithm: %s\n# Created: %s\n# Generated by: Hashana",
				hashString, selectedFile.getName(), algorithm, timestamp);
		content = String.format("%s  %s\n# Algorithm: %s\n# Created: %s\n# Generated using: Hashana",
				hashString, selectedFile.getName(), algorithm, timestamp);


		try {
			Files.write(checksumFilePath, content.getBytes(StandardCharsets.UTF_8));
			statusArea.appendText("\nChecksum file saved: " + checksumFilePath.toString());
			DialogUtils.createStyledAlert(Alert.AlertType.INFORMATION, "Success", "Checksum File Created", "Checksum file saved successfully at:\n" + checksumFilePath.toString()).showAndWait();
		} catch (IOException e) {
			statusArea.appendText("\nError saving checksum file: " + e.getMessage());
			DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "Save Error", "Error Saving Checksum File", "Could not save the checksum file: " + e.getMessage()).showAndWait();
		}
	}



	@FXML
	private void handleCancel(ActionEvent event) {
		cancelRequested = true;
		statusArea.appendText("\nCancellation requested...");
		cancelButton.setDisable(true);
	}

	private void updateProgress(long bytesRead, long totalBytes) {
		Platform.runLater(() -> {
			double progress = (double) bytesRead / totalBytes;
			hashProgress.setProgress(progress);
			progressLabel.setText(String.format("%s / %s (%.1f%%)",
					formatBytes(bytesRead),
					formatBytes(totalBytes),
					progress * 100
			));
		});
	}
	private String formatBytes(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp-1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	private void resetProgress() {
		Platform.runLater(() -> {
			hashProgress.setProgress(0);
			progressLabel.setText("0%");
		});
	}
	private void resetUIState() {
		cancelButton.setDisable(true);
		createChecksumButton.setDisable(false);
		browseButton.setDisable(false);
	}

}
