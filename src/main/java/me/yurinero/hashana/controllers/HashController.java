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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashFunction;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import me.yurinero.hashana.utils.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;

/* This is the controller for the Hash tab.
*  We can expand it by adding further algo's to the hashes string and HashUtil's class.
*/


public class HashController implements Initializable {

	// UI Components
	public TextArea hashOutputField;
	public TextArea hashInfoField;
	@FXML
	private ChoiceBox<String> hashChoice; // Use generics for type safety
	@FXML
	private TextArea textInputArea;

	// Controller specific fields
	private final String[] hashes = {"SHA256", "SHA384", "SHA512","MD5","SIPHASH24"};
	private  static final Logger logger = LoggerFactory.getLogger(HashController.class);

	//
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		hashChoice.getItems().addAll(hashes);
		hashChoice.setValue(hashes[0]); // Set default selection
		loadAlgorithmInfo();
		setupInfoListener();
		updateAlgorithmInfo(hashes[0]);
	}
	// Controller specific methods

	@FXML
	private void handleHashButtonClick(ActionEvent event) {
		String userInput = textInputArea.getText();
		String algorithm = hashChoice.getValue();

		HashFunction hashFunction = getHashFunction(algorithm);
		if (hashFunction == null) {
			hashOutputField.setText("Error: Unsupported algorithm selected");
			logger.error("Error: Unsupported algorithm selected");
			return;
		}

		// Compute hash using UTF-8 encoding
		byte[] inputBytes = userInput.getBytes(StandardCharsets.UTF_8);
		String hashResult = hashFunction.hashBytes(inputBytes).toString();

		hashOutputField.setText(hashResult);
	}

	private HashFunction getHashFunction(String algorithm) {
		return HashUtils.getHashFunction(algorithm);
	}
	private Map<String, AlgorithmInfo> algorithmInfoMap = new HashMap<>();
	private void setupInfoListener() {
		hashChoice.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> updateAlgorithmInfo(newVal)
		);
	}
   //Loads .json file used to parse information about the subject algorithm
	private void loadAlgorithmInfo() {
		try {
			// Use class loader for reliable resource access
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream("algorithm_info.json");

			if (is == null) {
				throw new IOException("File not found in resources!");
			}

			ObjectMapper mapper = new ObjectMapper();
			algorithmInfoMap = mapper.readValue(is, new TypeReference<>() {});
		} catch (IOException e) {
			Platform.runLater(() ->
					hashInfoField.setText("ERROR: Failed to load algorithm info\n" + e.getMessage())

			);
			logger.error("Failed to load algorithm info: {}", e.getMessage());
		}
	}
    //Parses the information contained within the .json file to @hashInfoField. Can be expanded by adding further fields into @AlgorithmInfo and then joining them here.
	private void updateAlgorithmInfo(String algorithm) {
		AlgorithmInfo info = algorithmInfoMap.getOrDefault(algorithm, new AlgorithmInfo());

		StringJoiner sj = new StringJoiner("\n\n");
		sj.add("Algorithm: " + (info.name != null ? info.name : algorithm.toUpperCase()));
		if (info.year != null) sj.add("Year: " + info.year);
		if (info.history != null) sj.add("History:\n" + info.history);
		if (info.security != null) sj.add("Security:\n" + info.security);
		if (info.usage != null) sj.add("Usage:\n" + info.usage);
		if (info.logic != null ) sj.add("Logic:\n" + info.logic);

		hashInfoField.setText(sj.toString());
	}

	private static class AlgorithmInfo {
		public String name;
		public Integer year;
		public String history;
		public String security;
		public String usage;
		public String logic;
	}
}
