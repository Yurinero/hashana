package me.yurinero.hashana.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Helper utility class for custom styled Alert dialogs.
public class DialogUtils {

	/**
	 *
	 * @param alertType
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert createStyledAlert(Alert.AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		DialogPane dialogPane = alert.getDialogPane();
		String cssPath = DialogUtils.class.getResource("/me/yurinero/hashana/dark-theme.css").toExternalForm();
		if (cssPath != null) {
			dialogPane.getStylesheets().add(cssPath);
		} else {
			System.err.println("cssPath is null");
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
