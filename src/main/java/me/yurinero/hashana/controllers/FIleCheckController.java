package me.yurinero.hashana.controllers;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import me.yurinero.hashana.utils.HashUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FIleCheckController extends FileOperationController {
	// UI Components
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
	// Controller specific Fields
	private final String[] fileHashAlgorithms = {"SHA256", "SHA384", "SHA512", "MD5"};
	private  static final Logger logger = LoggerFactory.getLogger(FIleCheckController.class);
	private static final String HELP_TEXT_CONTENT =
			"""
					How to Use:
					
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
		// Initialize abstract base controller
		super.initialize();
		//Populate list with hashing algorithms from fileHashAlgorithms
		fileHashChoice.getItems().addAll(fileHashAlgorithms);
		//Set default SHA256 algorithm
		fileHashChoice.setValue(fileHashAlgorithms[0]);
		//Add Accelerator aka Shortcut for File Browsing and Cancellation of ongoing File Hash Check
		addAccelerator(KeyCode.O, KeyCombination.CONTROL_DOWN,() -> browseButton.fire());
		addAccelerator(KeyCode.X, KeyCombination.CONTROL_DOWN,() -> cancelButton.fire());
		//Initialize the help text
		loadHelpText();
	}

	// Implementation of Abstract methods
	@Override
	protected void onFileSelected(File file) {
		autoDetectChecksumFile();
	}

	@Override
	protected void onFileSelectionCancelled() {
	}

	@Override protected TextField getFilePathField() { return filePathField; }
	@Override protected ProgressBar getProgressBar() { return hashProgress; }
	@Override protected Label getProgressLabel() { return progressLabel; }
	@Override protected Button getCancelButton() { return cancelButton; }
	@Override protected AnchorPane getRootPane() { return rootAnchor; }

	// Controller Specific methods

	// Load the HELP_TEXT_CONTENT into the Text Area
	private void loadHelpText() {
		if (helpTextArea != null) {
			helpTextArea.setText(HELP_TEXT_CONTENT);
		}
	}

	// Look for presence of checksum file
	private void autoDetectChecksumFile() {
		// Look for common checksum file extensions
		String[] extensions = {".md5", ".sha256", ".sha384", ".sha512"};
		for (String ext : extensions) {
			File checksumFile = new File(selectedFile.getAbsolutePath() + ext);
			if (checksumFile.exists()) {
				loadChecksumFromFile(checksumFile);
				logger.debug("Checksum file found: {}", checksumFile.getAbsolutePath());
				break;
			}
		}
	}

	private void loadChecksumFromFile(File checksumFile) {
		try {
			List<String> lines = Files.readAllLines(checksumFile.toPath());
			if (!lines.isEmpty()) {
				String[] parts = lines.get(0).split("\\s+");
				expectedHashField.setText(parts[0]);
				logger.debug("Loaded checksum from file: {}", checksumFile.getAbsolutePath());
			}
		} catch (IOException e) {
			showError("Error reading checksum file: " + e.getMessage());
			logger.error("Error reading checksum file: {}", e.getMessage());
		}
	}

	// Calculate the hash of the file by loading it into memory in chunks based on the selected algorithm.
	@FXML
	private void handleCalculateHash(ActionEvent event) {
		if (selectedFile == null) {
			showError("Please select a file first!");
			return;
		}

		ExecutorService executor = ThreadPoolService.getInstance().getExecutorService();
		getCancelButton().setDisable(false);
		resetProgress();
		cancelRequested = false;

		HashFunction hashFunction = HashUtils.getHashFunction(fileHashChoice.getValue());

		executor.submit(() -> {
			try (InputStream is = new FileInputStream(selectedFile)) {
				final HashCode hashCode = hashStream(is, hashFunction);

				if (hashCode != null) {
					Platform.runLater(() -> {
						computedHashField.setText(hashCode.toString());
						autoVerifyIfNeeded();
						getProgressBar().setProgress(1.0);
						getCancelButton().setDisable(true);
						getProgressLabel().setText("Complete! (" + formatBytes(selectedFile.length()) + ")");
						logger.debug("Calculated hash successfully");
					});
				}
			} catch (IOException e) {
				Platform.runLater(() -> showError("Error reading file: " + e.getMessage()));
				logger.error("Error reading file: {}", e.getMessage());
			} finally {
				if (cancelRequested) {
					Platform.runLater(this::resetProgress);
				}
				Platform.runLater(() -> getCancelButton().setDisable(true));
			}
		});
	}

	// If Expected Hash field has been entered before starting the operation, automatically compare to the result.
	private void autoVerifyIfNeeded() {
		if (!expectedHashField.getText().isEmpty()) {
			handleVerify(null);
		}
	}

	// Compare the two hashes
	@FXML
	private void handleVerify(ActionEvent event) {
		String computed = computedHashField.getText().trim().toLowerCase();
		String expected = expectedHashField.getText().trim().toLowerCase();

		if (computed.isEmpty() || expected.isEmpty()) {
			verificationStatus.setText("Please provide both hashes for verification");
			logger.debug("Hashes for verification are empty");
			return;
		}

		if (computed.equals(expected)) {
			verificationStatus.setText("✓ Hashes match!");
			logger.debug("Hashes match.");
			verificationStatus.setStyle("-fx-text-fill: green;");
		} else {
			verificationStatus.setText("✗ Hashes do not match!");
			verificationStatus.setStyle("-fx-text-fill: red;");
			logger.debug("Hashes do not match.");
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
