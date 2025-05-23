package me.yurinero.hashana.controllers;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;


/* This class handles the File Hash Check tab.
* In short, it is absolute fucking voodoo.
*/


public class FileCheck {
	public TextField filePathField;
	public Button browseButton;
	public ChoiceBox<String> fileHashChoice;
	public Button calculateHashButton;
	public TextField computedHashField;
	public TextField expectedHashField;
	public Button verifyButton;
	public Label verificationStatus;
	public ProgressBar hashProgress;
	public Label progressLabel;
	public Button cancelButton;
	private File selectedFile;

	private final String[] fileHashAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};

	private volatile boolean cancelRequested = false;
	private static final int UPDATE_INTERVAL_MS = 100;
	private long lastUpdateTime = 0;

	@FXML
	public void initialize() {
		//Populate list with hashing algorithms from fileHashAlgorithms
		fileHashChoice.getItems().addAll(fileHashAlgorithms);
		//Set default SHA256 algorithm
		fileHashChoice.setValue(fileHashAlgorithms[0]);
		//Disable the cancel button, so it cannot be spammed
		cancelButton.setDisable(true);
	}

	@FXML
	private void handleFileBrowse(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File for Hashing");
		selectedFile = fileChooser.showOpenDialog(filePathField.getScene().getWindow());

		if (selectedFile != null) {
			filePathField.setText(selectedFile.getAbsolutePath());
			// Optional: Auto-detect checksum files
			autoDetectChecksumFile();
		}
	}

	private void autoDetectChecksumFile() {
		// Look for common checksum file extensions
		String[] extensions = {".md5", ".sha256", ".sha384", ".sha512"};
		for (String ext : extensions) {
			File checksumFile = new File(selectedFile.getAbsolutePath() + ext);
			if (checksumFile.exists()) {
				loadChecksumFromFile(checksumFile);
				break;
			}
		}
	}

	private void loadChecksumFromFile(File checksumFile) {
		try {
			List<String> lines = Files.readAllLines(checksumFile.toPath());
			if (!lines.isEmpty()) {
				// Typical format: "CHECKSUM  FILENAME"
				String[] parts = lines.get(0).split("\\s+");
				expectedHashField.setText(parts[0]);
			}
		} catch (IOException e) {
			showError("Error reading checksum file: " + e.getMessage());
		}
	}


	@FXML
	private void handleCalculateHash(ActionEvent event) {
		if (selectedFile == null) {
			showError("Please select a file first!");
			return;
		}
		ExecutorService executor = ThreadPoolService.getInstance().getExecutorService();
		//Enable the cancel button when operation starts
		cancelButton.setDisable(false);
		//Resets the progress bar
		resetProgress();
		cancelRequested = false;
		HashFunction hashFunction = HashUtils.getHashFunction(fileHashChoice.getValue());
        //Handles the actual logic of getting the file hash on a new thread. The Guava methods used are labeled experimental and should be treated as such.
		executor.submit(() ->{
			try (InputStream is = new FileInputStream(selectedFile)) {
				long fileSize = selectedFile.length();
				Hasher hasher = hashFunction.newHasher();
				// Size of the buffer used during the calculation. Loads the file in chunks.
				byte[] buffer = new byte[64 * 1024]; // 64KB buffer
				long bytesRead = 0;
				int read;

				updateProgress(0, fileSize);

				while ((read = is.read(buffer)) != -1 && !cancelRequested) {
					hasher.putBytes(buffer, 0, read);
					bytesRead += read;

					long now = System.currentTimeMillis();
					if (now - lastUpdateTime > UPDATE_INTERVAL_MS || bytesRead == fileSize) {
						updateProgress(bytesRead, fileSize);
						lastUpdateTime = now;
					}
				}

				if (!cancelRequested) {
					HashCode hashCode = hasher.hash();
					Platform.runLater(() -> {
						computedHashField.setText(hashCode.toString());
						autoVerifyIfNeeded();
						hashProgress.setProgress(1.0);
						//Disable cancel button if operation completed successfully
						cancelButton.setDisable(true);
						progressLabel.setText("Complete! (" + formatBytes(fileSize) + ")");
					});
				}
			} catch (IOException e) {
				Platform.runLater(() -> showError("Error reading file: " + e.getMessage()));
			} finally {
				if (cancelRequested) {
					//Disable cancel button if cancel action is requested
					cancelButton.setDisable(true);
					Platform.runLater(this::resetProgress);
				}
			}
		});
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
	private void resetProgress() {
		hashProgress.setProgress(0);
		progressLabel.setText("0 B / 0 B (0.0%)");
		hashProgress.setStyle("");
	}
	@FXML
	private void handleCancel(ActionEvent event) {
		cancelRequested = true;
		cancelButton.setDisable(true);
		progressLabel.setText("Cancelled - " + progressLabel.getText());
		hashProgress.setStyle("-fx-accent: #ff4444;");
	}

	private String formatBytes(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp-1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}


	private void autoVerifyIfNeeded() {
		if (!expectedHashField.getText().isEmpty()) {
			handleVerify(null);
		}
	}
	@FXML
	private void handleVerify(ActionEvent event) {
		String computed = computedHashField.getText().trim().toLowerCase();
		String expected = expectedHashField.getText().trim().toLowerCase();

		if (computed.isEmpty() || expected.isEmpty()) {
			verificationStatus.setText("Please provide both hashes for verification");
			return;
		}

		if (computed.equals(expected)) {
			verificationStatus.setText("✓ Hashes match!");
			verificationStatus.setStyle("-fx-text-fill: green;");
		} else {
			verificationStatus.setText("✗ Hashes do not match!");
			verificationStatus.setStyle("-fx-text-fill: red;");
		}
	}

	private void showError(String message) {
		verificationStatus.setText(message);
		verificationStatus.setStyle("-fx-text-fill: red;");
	}
	private HashFunction getHashFunction(String algorithm) {
		return HashUtils.getHashFunction(algorithm);
	}
}
