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
	@Override protected AnchorPane getRootPane() { return rootAnchor; }

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

		ExecutorService executor = ThreadPoolService.getInstance().getExecutorService();
		executor.submit(() -> createHashInBackground(hashFunction,algorithm));
	}

	private void createHashInBackground(HashFunction hashFunction, String algorithm) {
		try (InputStream is = new FileInputStream(selectedFile)) {
			HashCode hashCode = hashStream(is, hashFunction);

			if (hashCode != null) {
				Platform.runLater(() -> {
					String hashString = hashCode.toString();
					statusArea.appendText("\nHash calculated: " + hashString);
					saveChecksumFile(hashString, algorithm);
				});

			} else {
				Platform.runLater(() -> statusArea.appendText("\nHash calculation cancelled by user."));
			}

		} catch (IOException e) {
			Platform.runLater(() -> {
				statusArea.appendText("\nError reading file: " + e.getMessage());
				DialogUtils.createStyledAlert(Alert.AlertType.ERROR, "File Error", "Error Reading File", "Could not read the selected file: " + e.getMessage()).showAndWait();
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
