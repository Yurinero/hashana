package me.yurinero.hashana.controllers;

import com.google.common.hash.HashFunction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import me.yurinero.hashana.utils.HashUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

public class PasswordGeneratorController {
	// UI Components
	public TextField passwordOutput;
	public Button generateButton;
	public CheckBox usualSymbols;
	public CheckBox unusualSymbols;
	public CheckBox numberCheckbox;
	public Slider lengthSlider;
	public TextField lengthDisplay;
	public Button copyButton;
	public CheckBox upperCaseCheckbox;
	public CheckBox lowerCaseCheckbox;
	public Region entropyPad;
	public CheckBox useEntropyCheckbox;
	public Label passwordInfoLabel;
	public CheckBox guaranteeCheckbox;

	// Controller specific fields
	private final SecureRandom randomizer = new SecureRandom();
	private final StringBuilder entropyCollector = new StringBuilder();
	private static final int MAX_ENTROPY_LENGTH = 4096;
	private final Color startColor = Color.web("#e8f4ff");
	private final Color endColor = Color.web("#aaccff");


	@FXML
	public void initialize() {
		// Set up slider constraints
		lengthSlider.setMin(6);
		lengthSlider.setMax(32);
		lengthSlider.setBlockIncrement(1);


		// Bind slider and text field
		lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			int value = newVal.intValue();
			lengthDisplay.setText(String.valueOf(value));
		});

		lengthDisplay.textProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.matches("\\d*")) {
				int value = newVal.isEmpty() ? 6 : Math.min(32, Math.max(6, Integer.parseInt(newVal)));
				lengthSlider.setValue(value);
			} else {
				lengthDisplay.setText(oldVal);
			}
		});
		// Password length value is 16 by default
		lengthSlider.setValue(16);
		// Set up the entropy collection functionality
		setupEntropyCollector();
		// Choice for one of each category is enabled by default
		guaranteeCheckbox.setSelected(true);
	}

	/**
	 * Sets up the logic for the entropy pad, including enabling/disabling it,
	 * capturing mouse movement data, and providing visual feedback.
	 */
	private void setupEntropyCollector() {
		// Entropy pad is disabled until the user opts into the feature
		entropyPad.setDisable(true);
		entropyPad.setStyle("-fx-background-color: #cccccc; -fx-border-color: #aaaaaa; -fx-border-radius: 5;");

		useEntropyCheckbox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
			entropyPad.setDisable(!isNowSelected);
			// Clear any old data on toggle
			entropyCollector.setLength(0);

			if (isNowSelected) {
				// Reset to the starting color and provide instructions
				updateEntropyPadColor(0.0);
				passwordInfoLabel.setText("Move your mouse over the highlighted area to add randomness.");
			} else {
				// Revert to disabled style
				entropyPad.setStyle("-fx-background-color: #cccccc; -fx-border-color: #aaaaaa; -fx-border-radius: 5;");
			}
		});

		// This is the core of the entropy collection.
		// We listen for mouse movements over the entropyPad region.
		entropyPad.setOnMouseMoved((event) -> {
			// Only collect data if the feature is enabled.
			if (useEntropyCheckbox.isSelected()) {
				// Stop collecting if the entropy cap is reached
				if (entropyCollector.length() >= MAX_ENTROPY_LENGTH) {
					passwordInfoLabel.setText("Ready to generate. Maximum entropy reached: " + MAX_ENTROPY_LENGTH);
					return;
				}
				// High-precision timer for more randomness
				long time = System.nanoTime();
				// Mouse X coordinate
				double x = event.getX();
				// Mouse Y coordinate
				double y = event.getY();

				// Append the coordinates and timestamp to our collector.
				entropyCollector.append(x).append(y).append(time);
				// Update the visual feedback based on how full the collector is
				double progress = (double) entropyCollector.length() / MAX_ENTROPY_LENGTH;
				updateEntropyPadColor(progress);
			}
		});

	}

	/**
	 * Updates the background color of the entropyPad based on the collection progress.
	 * @param progress A value from 0.0 (empty) to 1.0 (full).
	 */
	private void updateEntropyPadColor(double progress) {
		Color interpolatedColor = startColor.interpolate(endColor, progress);
		entropyPad.setStyle(String.format("-fx-background-color: %s; -fx-border-color: #89b4fa; -fx-border-radius: 5;", toWebColor(interpolatedColor)));
	}

	private String toWebColor(Color color) {
		return String.format("#%02x%02x%02x",
				(int) (color.getRed() * 255),
				(int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	/**
	 * Converts a JavaFX Color object to a CSS-friendly hex string (e.g., "#RRGGBB").
	 */
	@FXML
	private void handleGenerateButton() {
		// Process and apply user generated entropy
		if (useEntropyCheckbox.isSelected()) {
			// Ensure the user has moved the mouse over the pad.
			if (entropyCollector.isEmpty()) {
				passwordInfoLabel.setText("Please move mouse over the entropy pod first!");
				return;
			}
			// Get the collected data as a single string
			String entropyData = entropyCollector.toString();
			// Use the SHA256 hashing function to process the arbitrary data into a 32 byte seed.
			HashFunction hf = HashUtils.getHashFunction("SHA256");
			byte[] seed = hf.hashString(entropyData, StandardCharsets.UTF_8).asBytes();
			// Supplement SecureRandom with our new seed.
			randomizer.setSeed(seed);
			// Reset the collector and Pad colour indicator for the next run.
			entropyCollector.setLength(0);
			updateEntropyPadColor(0.0);
		}


		// Validate at least one checkbox selected
		if (!anyCheckboxSelected()) {
			passwordInfoLabel.setText("Select at least one option!");
			return;
		}
		// Gets the desired password length from the slider.
		int passwordLength = (int) lengthSlider.getValue();
		// Calls the password generation method and limits output based on the slider length
		String password = generatePassword(passwordLength);
		// Output the result into the relevant Text field
		passwordOutput.setText(password);
	}

	// Small helper function to copy the output into the users clipboard
	@FXML
	private void handleCopyButton() {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(passwordOutput.getText());
		clipboard.setContent(content);
	}

	// Check which checkboxes are ticked and therefore desired options are selected
	private boolean anyCheckboxSelected() {
		return usualSymbols.isSelected() || numberCheckbox.isSelected() || unusualSymbols.isSelected() || upperCaseCheckbox.isSelected() || lowerCaseCheckbox.isSelected ();
	}

	private String generatePassword(int length) {
		// Character pools
		StringBuilder pool = new StringBuilder();
		List<String> categories = new ArrayList<>();
		// 10 generation re-union of if statements because fuck me
		if (usualSymbols.isSelected()) {
			String letters = "!@#$%^&*()-_=+";
			pool.append(letters);
			categories.add(letters);
		}
		if (numberCheckbox.isSelected()) {
			String numbers = "0123456789";
			pool.append(numbers);
			categories.add(numbers);
		}
		if (unusualSymbols.isSelected()) {
			String symbols = "[]{}|;:'\",.<>/?";
			pool.append(symbols);
			categories.add(symbols);
		}
		if (upperCaseCheckbox.isSelected()) {
			String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			pool.append(upperCase);
			categories.add(upperCase);
		}
		if (lowerCaseCheckbox.isSelected()) {
			String lowerCase = "abcdefghijklmnopqrstuvwxyz";
			pool.append(lowerCase);
			categories.add(lowerCase);
		}

		// Build password with at least one from each category if checkbox is selected
		if (guaranteeCheckbox.isSelected()) {
				List<Character> passwordChars = new ArrayList<>();

			// Add mandatory characters
			for (String category : categories) {
				passwordChars.add(category.charAt(randomizer.nextInt(category.length())));
			}
			// Fill remaining characters
			for (int i = passwordChars.size(); i < length; i++) {
				passwordChars.add(pool.charAt(randomizer.nextInt(pool.length())));
			}
			// Shuffle and convert to string
			Collections.shuffle(passwordChars, randomizer);
			StringBuilder result = new StringBuilder();
			for (char c : passwordChars) {
				result.append(c);
			}

			return result.toString();
		} else {
			// Generate from the pool without guarantee
			StringBuilder password = new StringBuilder(length);
			for (int i = 0;i < length; i++){
				password.append(pool.charAt(randomizer.nextInt(pool.length())));
			}
			return password.toString();
		}
	}
}