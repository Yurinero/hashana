package me.yurinero.hashana.utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LicenseUtil {
	public TextArea licenseText;
	public Button licenseAccept;
	public Button licenseDeny;
	public HBox licenseTitle;
	private Stage stage;
	private Scene mainScene;

	public void setMainScene(Scene mainScene) {
		this.mainScene = mainScene;
	}
	public void setStage(Stage stage) {
		this.stage = stage;

		WindowUtils.setupDragging(this.stage, licenseTitle);
		WindowUtils.setupCloseButton(this.stage, licenseAccept);
	}
}
