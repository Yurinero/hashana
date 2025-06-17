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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.yurinero.hashana.controllers.MainViewController;
import me.yurinero.hashana.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;


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
		UserSettings.SettingsData appSettings = UserSettings.getInstance().getSettings();
		// Checks if the license agreement has been accepted
		if (!appSettings.acceptedLicense) {
			// If not, show the license agreement window and wait for the user's action.
			boolean licenseAccepted = showLicenseAgreement();

			// If the license was not accepted (e.g., the method returns false), exit the application.
			if (!licenseAccepted) {
				Platform.exit();
				System.exit(0);
				// Stop further execution.
				return;
			}
		}

		FXMLLoader fxmlLoader = new FXMLLoader(Hashana.class.getResource("view/main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
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

		stage.setScene(scene);
		// Needed to pass the stage to the MainViewController
		MainViewController controller = fxmlLoader.getController();
		controller.setStage(stage);
		// This is also for the system tray, because we need two, apparently. Words fail me.
		stage.getIcons().add(new Image(Objects.requireNonNull(Hashana.class.getResourceAsStream("hashana-icon.png"))));
		setupSystemTray(stage);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("Hashana");
		stage.show();



	}
	/**
	 * Loads and displays the license agreement window modally.
	 * @return true if the license was accepted, false otherwise.
	 */
	private boolean showLicenseAgreement() {
		try {
			// First, check if the FXML file can be found.
			URL fxmlUrl = getClass().getResource("/me/yurinero/hashana/view/license-view.fxml");
			if (fxmlUrl == null) {
				logger.error("FATAL: Could not find FXML resource: /me/yurinero/hashana/license-view.fxml");
				return false;
			}

			// If found, try to load it. This is where a syntax error in the FXML would cause an exception.
			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			Parent root = loader.load();

			Stage licenseStage = new Stage();
			licenseStage.setTitle("License Agreement");
			licenseStage.initModality(Modality.APPLICATION_MODAL);
			licenseStage.initStyle(StageStyle.UNDECORATED);

			Scene licenseScene = new Scene(root);

			// Apply theme to the license window
			String currentTheme = UserSettings.getInstance().getSettings().activeTheme;
			String cssPath = ThemeUtils.getCssPathForTheme(currentTheme);
			URL cssUrl = getClass().getResource(cssPath);
			if (cssUrl != null) {
				licenseScene.getStylesheets().add(cssUrl.toExternalForm());
			} else {
				logger.warn("Could not find CSS for license window: {}", cssPath);
			}

			LicenseUtil controller = loader.getController();
			controller.setStage(licenseStage);

			licenseStage.setScene(licenseScene);
			licenseStage.showAndWait();

		} catch (Exception e) {
			// This will catch ANY exception during the process (file not found, FXML error, etc.)
			// and log it with a full stack trace.
			logger.error("A critical exception occurred while loading the license screen.", e);
			// Ensure the app exits if the license screen fails to load.
			return false;
		}

		// After the window is closed, check the flag to confirm acceptance.
		return UserSettings.getInstance().getSettings().acceptedLicense;
	}

	// This is for the system tray icon. Doesn't work that well Linux...too bad!
	private void setupSystemTray(Stage stage) {
		// Ensure the platform toolkit is initialized
		java.awt.Toolkit.getDefaultToolkit();

		// Check if the system tray is supported
		if (!java.awt.SystemTray.isSupported()) {
			logger.error("System tray is not supported");
			return;
		}

		try {
			java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
			// Load the small tray icon image
			java.awt.Image image = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResource("/me/yurinero/hashana/tray-icon.png")));

			// Create the tray icon
			java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "Hashana");
			trayIcon.setImageAutoSize(true);

			java.awt.PopupMenu popup = new java.awt.PopupMenu();
			java.awt.MenuItem showItem = new java.awt.MenuItem("Show");
			showItem.addActionListener(e -> Platform.runLater(stage::show));
			java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
			exitItem.addActionListener(e -> Platform.exit());
			popup.add(showItem);
			popup.add(exitItem);
			trayIcon.setPopupMenu(popup);

			// Add the icon to the system tray
			tray.add(trayIcon);
		} catch (Exception e) {
			// Log the error
			logger.error("A critical exception occurred while loading the system tray.", e);
		}
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