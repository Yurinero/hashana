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
package me.yurinero.hashana.utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LicenseUtil {
	public Button licenseAccept;
	public Button licenseDeny;
	public HBox licenseTitle;
	public Label licenseLabel;
	private Stage stage;
	private Scene mainScene;

	@FXML
	public void initialize() {
		//  Button Actions
		licenseDeny.setOnAction(event -> {
			Platform.exit(); // Exit the application entirely.
			System.exit(0);
		});

		licenseAccept.setOnAction(event -> {
			// Set the flag to true and save the settings.
			UserSettings.SettingsData settings = UserSettings.getInstance().getSettings();
			settings.acceptedLicense = true;
			UserSettings.getInstance().saveSettings();

			// Close the license window.
			if (stage != null) {
				stage.close();
			}
		});

		// Load License Text
		try (InputStream is = getClass().getResourceAsStream("/me/yurinero/hashana/LICENSE")) {
			if (is != null) {
				String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
				licenseLabel.setText(text);
			} else {
				licenseLabel.setText("LICENSE file not found in application resources.");
			}
		} catch (IOException e) {
			licenseLabel.setText("An error occurred while trying to load the license text.");
		}


	}



	public void setMainScene(Scene mainScene) {
		this.mainScene = mainScene;
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}
}
