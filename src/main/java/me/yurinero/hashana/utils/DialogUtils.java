package me.yurinero.hashana.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

// Helper utility class for custom styled Alert dialogs.
public class DialogUtils {

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
			System.err.println("DialogUtils: UserSettings or activeTheme is null, defaulting to Dark theme for dialog.");
		}
		String cssPath = ThemeUtils.getCssPathForTheme(activeTheme);
		try {
			URL cssURL = DialogUtils.class.getResource(cssPath);
			if (cssURL != null) {
				dialogPane.getStylesheets().add(cssURL.toExternalForm());
			} else {
				System.err.println("DialogUtils: CSS file path not found: " + cssPath);
				URL fallBackCssURL = DialogUtils.class.getResource(ThemeUtils.getCssPathForTheme("Dark"));
				if (fallBackCssURL != null) {
					dialogPane.getStylesheets().add(fallBackCssURL.toExternalForm());
					System.err.println("DialogUtils: Applied fallback Dark theme to dialog.");
				} else {
					System.err.println("DialogUtils: Fallback Dark theme also not found.");
				}
			}
		} catch (Exception e) {
			System.err.println("DialogUtils: Error applying theme CSS " + cssPath + "to dialog: " + e.getMessage());
			e.printStackTrace();
		}

		Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
		if (dialogStage != null) {
			dialogStage.initStyle(StageStyle.UNDECORATED);
		} else {
			dialogPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
				if (newScene != null) {
					Stage stage = (Stage) newScene.getWindow();
					if (stage != null && !stage.isShowing()) {
						try {
							stage.initStyle(StageStyle.UNDECORATED);
						} catch (IllegalStateException e) {
							System.err.println("Could not set undecorated style, stage might be already managed: " + e.getMessage());
						}
					}
				}
			});
		}

		return alert;
	}
}
