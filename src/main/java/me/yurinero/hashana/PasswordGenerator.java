package me.yurinero.hashana;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.security.SecureRandom;
import java.util.*;

public class PasswordGenerator {
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
	private final SecureRandom randomizer = new SecureRandom();



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
	}

	@FXML
	private void handleGenerateButton() {
		// Validate at least one checkbox selected
		if (!anyCheckboxSelected()) {
			passwordOutput.setText("Select at least one option!");
			return;
		}

		int passwordLength = (int) lengthSlider.getValue();
		String password = generatePassword(passwordLength);
		passwordOutput.setText(password);
	}

	@FXML
	private void handleCopyButton() {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(passwordOutput.getText());
		clipboard.setContent(content);
	}

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

		// Build password with at least one from each category
		List<Character> password = new ArrayList<>();

		// Add mandatory characters
		for (String category : categories) {
			password.add(category.charAt(randomizer.nextInt(category.length())));
		}

		// Fill remaining characters
		for (int i = password.size(); i < length; i++) {
			password.add(pool.charAt(randomizer.nextInt(pool.length())));
		}

		// Shuffle and convert to string
		Collections.shuffle(password, randomizer);
		StringBuilder result = new StringBuilder();
		for (char c : password) result.append(c);
		return result.toString();
	}
}