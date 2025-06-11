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

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/* Shows a select image, usually called a splash screen to the user upon launching the app.
* In our case right now this is pretty useless since we can launch in <500ms and aren't loading anything in the background.
* Credit for implementation goes to Edward Stephen Jr. > https://coderscratchpad.com/creating-splash-screens-with-javafx/
*/

public class SplashScreen extends Preloader {

	// Create the splash screen layout
	private final StackPane parent = new StackPane();

	private Stage preloaderStage;

	@Override
	public void init() throws Exception {


		// Load the image to be displayed on the splash screen
		Image image = new Image("hashana-splash.png");

		// Create an ImageView to display the image
		ImageView imageView = new ImageView(image);

		// Preserve the image's aspect ratio
		imageView.setPreserveRatio(true);

		// Set the width of the ImageView
		imageView.setFitWidth(500);

		// Add the ImageView to the parent StackPane
		this.parent.getChildren().add(imageView);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.preloaderStage = stage;

		// Create a scene with the StackPane as the root
		Scene scene = new Scene(parent, 640, 480);

		// Make the scene background transparent
		scene.setFill(Color.TRANSPARENT);

		// Set the scene for the stage
		stage.setScene(scene);

		// Remove window decorations
		stage.initStyle(StageStyle.TRANSPARENT);

		// Center the SplashScreen on the screen
		stage.centerOnScreen();

		// Display the SplashScreen
		stage.show();
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification info) {

		if (info.getType() == StateChangeNotification.Type.BEFORE_START) {

			// Close the splash screen when the application is ready to start
			this.preloaderStage.close();
		}
	}
}