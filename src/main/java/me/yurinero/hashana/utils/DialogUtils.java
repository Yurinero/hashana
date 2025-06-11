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

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

// Helper utility class for custom styled Alert dialogs.
public class DialogUtils {
	private static final Logger logger = LoggerFactory.getLogger(DialogUtils.class);
	/**
	 * Creates a styled alert dialog that respects the application's current theme.
	 *
	 * @param alertType The type of alert (e.g., Alert.AlertType.ERROR).
	 * @param title     The title of the dialog window.
	 * @param header    The header text of the dialog.
	 * @param content   The main content message of the dialog.
	 * @return The configured Alert object.
	 */
	public static Alert createStyledAlert(Alert.AlertType alertType, String title, String header, String content) {
		logger.info("Creating styled alert: Title='{}', Header='{}', Type='{}'", title, header, alertType);
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		DialogPane dialogPane = alert.getDialogPane();

		UserSettings userSettings = UserSettings.getInstance();
		String activeTheme = "Dark";
		if (userSettings.getSettings() != null && userSettings.getSettings().activeTheme != null) {
			activeTheme = userSettings.getSettings().activeTheme;
		} else {
			logger.warn("UserSettings or activeTheme is null, defaulting to Dark theme for dialog.");
		}
		String cssPath = ThemeUtils.getCssPathForTheme(activeTheme);
		try {
			URL cssURL = DialogUtils.class.getResource(cssPath);
			if (cssURL != null) {
				dialogPane.getStylesheets().add(cssURL.toExternalForm());
				logger.debug("Successfully applied theme CSS: {}", cssPath);
			} else {
				logger.warn("DialogUtils: CSS file path not found: {}", cssPath);
				URL fallBackCssURL = DialogUtils.class.getResource(ThemeUtils.getCssPathForTheme("Dark"));
				if (fallBackCssURL != null) {
					dialogPane.getStylesheets().add(fallBackCssURL.toExternalForm());
					logger.info("DialogUtils: Applied fallback Dark theme to dialog.");
				} else {
					logger.error("DialogUtils: Fallback Dark theme also not found.");
				}
			}
		} catch (Exception e) {
			logger.error("DialogUtils: Error applying theme CSS {} to dialog: {}", cssPath, e.getMessage(), e);
		}

		Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
		if (dialogStage != null) {
			try {
				dialogStage.initStyle(StageStyle.UNDECORATED);
			} catch (IllegalStateException e) {
				logger.warn("Could not set undecorated style, stage might be already showing or managed: {}", e.getMessage());
			}
		} else {
			logger.debug("DialogPane scene or window not yet available for undecorated style. Adding listener.");
			dialogPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
				if (newScene != null) {
					Stage stage = (Stage) newScene.getWindow();
					if (stage != null && !stage.isShowing()) {
						try {
							stage.initStyle(StageStyle.UNDECORATED);
							logger.debug("Applied undecorated style via listener.");
						} catch (IllegalStateException e) {
							logger.warn("Could not set undecorated style via listener, stage might be already managed: {}", e.getMessage());
						}
					} else if (stage != null && stage.isShowing()) {
						logger.debug("Stage is already showing, undecorated style cannot be applied via listener.");
					}
				}
			});
		}

		return alert;
	}
}
