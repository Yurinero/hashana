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


package me.yurinero.hashana;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.yurinero.hashana.controllers.MainViewController;
import me.yurinero.hashana.utils.SplashScreen;
import me.yurinero.hashana.utils.ThemeUtils;
import me.yurinero.hashana.utils.ThreadPoolService;
import me.yurinero.hashana.utils.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;


public class Hashana extends Application {
	private static final Logger logger = LoggerFactory.getLogger(Hashana.class);

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
		String cssPath = ThemeUtils.getCssPathForTheme(activeTheme);

		try {
			URL cssURL = getClass().getResource(cssPath);
			if (cssURL != null) {
				scene.getStylesheets().add(cssURL.toExternalForm());
			} else {
				System.err.println("Couldn't find css path: " + cssPath);
				URL fallBackCssURL = Hashana.class.getResource(ThemeUtils.getCssPathForTheme("Dark"));
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
		logger.info("Hashana started");
	}
}