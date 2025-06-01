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
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import me.yurinero.hashana.utils.UserSettings;

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


public class FIleCheckController {
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
	public AnchorPane rootAnchor;
	public TextArea helpTextArea;
	private File selectedFile;



	private final String[] fileHashAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};
	private volatile boolean cancelRequested = false;
	private long lastUpdateTime = 0;
	private UserSettings.SettingsData appSettings;
	private static final String HELP_TEXT_CONTENT =
			"""
					How to Use File Hash Check:
					
					1. Browse: Click 'Browse' or use Ctrl+O to select a file.
					2. Select Algorithm: Choose a hashing algorithm (SHA256 is default).
					3. Calculate: Click 'Calculate Hash' to compute the file's hash.
					   - The progress bar will show the status.
					   - You can cancel the operation using 'Cancel' or Ctrl+X.
					4. Expected Hash (Optional): If you have an expected hash value, paste it into the 'Expected Hash' field.
					   - The application will try to auto-detect checksum files (e.g., .sha256) in the same directory.
					5. Verify: Click 'Verify' to compare the computed and expected hashes.
					   - The status will indicate if they match.
					
					Important Warning:
					This functionality uses hashing methods from Google Guava's library. Some of these methods might be labeled as experimental or beta by Guava. \
					Please be mindful of this when using the file hashing features for critical applications.""";

	@FXML
	public void initialize() {
		//Populate list with hashing algorithms from fileHashAlgorithms
		fileHashChoice.getItems().addAll(fileHashAlgorithms);
		//Set default SHA256 algorithm
		fileHashChoice.setValue(fileHashAlgorithms[0]);
		//Disable the cancel button, so it cannot be spammed
		cancelButton.setDisable(true);
		//Add Accelerator aka Shortcut for File Browsing and Cancellation of ongoing File Hash Check
		addAccelerator(KeyCode.O, KeyCombination.CONTROL_DOWN,() -> browseButton.fire());
		addAccelerator(KeyCode.X, KeyCombination.CONTROL_DOWN,() -> cancelButton.fire());
		//Load default/user settings
		appSettings = UserSettings.getInstance().getSettings();
		//
		loadHelpText();
	}
	private void loadHelpText() {
		if (helpTextArea != null) {
			helpTextArea.setText(HELP_TEXT_CONTENT);
			// Optionally, set a specific style or make it non-focusable if purely for display
			// helpInfoArea.setFocusTraversable(false);
			// helpInfoArea.setStyle("-fx-control-inner-background:#f0f0f0; -fx-text-fill: #333333;"); // Example style
		}
	}


	@FXML
	private void handleFileBrowse(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File for Hashing");
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
			autoDetectChecksumFile();
		}
		}
	}


	/** Generic method to add Accelerator's, effectively keyboard shortcuts.
	 *  Requires 3 values.
	 * @param keyCode  Key to be hit
	 * @param modifier  Modifier to be used, such as CTRL_DOWN
	 * @param action  Action to take upon completion
	 */

	public void addAccelerator(KeyCode keyCode,KeyCombination.Modifier modifier, Runnable action ) {
		KeyCombination keyCombination = new KeyCodeCombination(keyCode, modifier);
		rootAnchor.sceneProperty().addListener((observable, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.getAccelerators().put(keyCombination, action);
			}
		});
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
				byte[] buffer = new byte[appSettings.bufferSize * 1024]; // 64KB buffer default
				long bytesRead = 0;
				int read;

				updateProgress(0, fileSize);

				while ((read = is.read(buffer)) != -1 && !cancelRequested) {
					hasher.putBytes(buffer, 0, read);
					bytesRead += read;

					long now = System.currentTimeMillis();
					if (now - lastUpdateTime > appSettings.progressIntervalMS || bytesRead == fileSize) {
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
