package me.yurinero.hashana;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.yurinero.hashana.controllers.MainViewController;
import me.yurinero.hashana.utils.SplashScreen;
import me.yurinero.hashana.utils.ThreadPoolService;
import me.yurinero.hashana.utils.UserSettings;

import java.io.IOException;
import java.net.URL;


public class Hashana extends Application {
	@Override
	public void init() throws Exception {
		/* Sleeping the thread on purpose upon initialization of the application to display the splash screen.
		* This is absolutely fucking stupid and only done for aesthetics.*/
		UserSettings.SettingsData appSettings = UserSettings.getInstance().getSettings();
		if (appSettings.splashScreenEnabled) {
			Thread.sleep(2000);
		}
	}

	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(Hashana.class.getResource("main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());

		UserSettings.SettingsData appSettings = UserSettings.getInstance().getSettings();
		String activeTheme = appSettings.activeTheme;
		String cssPath = getCssPathForTheme(activeTheme);

		try {
			URL cssURL = getClass().getResource(cssPath);
			if (cssURL != null) {
				scene.getStylesheets().add(cssURL.toExternalForm());
			} else {
				System.err.println("Couldn't find css path: " + cssPath);
				URL fallBackCssURL = Hashana.class.getResource(getCssPathForTheme("Dark"));
				if (fallBackCssURL != null)scene.getStylesheets().add(fallBackCssURL.toExternalForm());
			}
		} catch (Exception e) {
			System.err.println("Couldn't find css path: " + cssPath);
		}


		// Needed to pass the stage to the MainViewController
		MainViewController controller = fxmlLoader.getController();
		controller.setStage(stage);

		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("Hashana");
		stage.setScene(scene);
		stage.show();



	}
	private String getCssPathForTheme(String themeName) {
		return switch (themeName) {
			case "Light" -> "/me/yurinero/hashana/light-theme.css";
			case "Accessible" -> "/me/yurinero/hashana/accessible-theme.css";
			case "Dark" -> "/me/yurinero/hashana/dark-theme.css"; // Default/fallback
			default -> {
				System.err.println("Unknown theme name: " + themeName + ". Defaulting to Dark.");
				yield "/me/yurinero/hashana/dark-theme.css";
			}
		};
	}

	// Calls the shutdown method from the ThreadPoolService to shut down any tasks running on background threads.
	public void stop(){
		ThreadPoolService.getInstance().shutdown();
	}
	public static void main(String[] args) {
		UserSettings.SettingsData appSettings = UserSettings.getInstance().getSettings();
		if (appSettings.splashScreenEnabled) {
			System.setProperty("javafx.preloader", SplashScreen.class.getName());
		}

		Application.launch(Hashana.class, args);
	}
}