package me.yurinero.hashana.utils;


import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;

import javafx.stage.Screen;
import javafx.stage.Stage;

/*
*   Helper class extracting existing window control logic to avoid duplication in separate classes.
*/
public class WindowUtils {

	/**
	 *  Makes an undecorated stage draggable using a specific node, in our case HBox but could be BorderPane or others.
	 * @param stage  The Stage to be moved.
	 * @param draggableNode The Node (e.g., HBox, BorderPane) that the user clicks and drags.
	 */
	public static void setupDragging(Stage stage, Node draggableNode) {
		/* Array to store offset values as Lambdas require the variable to be "final" or "effectively final."
		*  This way we satisfy the requirement while still being able to change the values.
		* xOffset = offsets[0], yOffset = offsets[1]
		*/
		final double[] offsets = new double[2];


		draggableNode.setOnMousePressed(event -> {
			offsets[0] = event.getSceneX();
			offsets[1] = event.getSceneY();
		});

		draggableNode.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - offsets[0]);
			stage.setY(event.getScreenY() - offsets[1]);
		});

	}

	/** Sets up a button to close the given stage.
	 *
	 * @param stage The stage to be closed.
	 * @param closeButton The button to trigger the action.
	 */
	public static void setupCloseButton(Stage stage, Button closeButton) {
		closeButton.setOnAction(event -> stage.close());
	}

	/** Sets up a button to minimize the stage.
	 *
	 * @param stage The stage to be minimized.
	 * @param minimizeButton The button to trigger the action.
	 */
	public static void setupMinimizeButton(Stage stage, Button minimizeButton) {
		minimizeButton.setOnAction(event -> stage.setIconified(true));
	}

	/** Sets up a button to toggle the stage between it's maximized and restored (original) state.
	 *
	 * @param stage The stage to be maximized.
	 * @param maximizeButton The button to trigger the action.
	 */
	public static void setupMaximizeRestore(Stage stage, Button maximizeButton) {
		maximizeButton.setOnAction(event -> {
			WindowState state = getWindowState(stage);
			if (state.maximized) {
				restoreWindow(stage, state);
			} else {
				maximizeWindow(stage, state);
			}
		});
	}

	/** Private helper class to hold the original state of a window.
	 */
	private static class WindowState {
		double originalX, originalY, originalWidth, originalHeight;
		boolean maximized = false;
	}


	private static void maximizeWindow(Stage stage, WindowState state) {
		if (stage == null) return;

		// Get the visual bounds of the primary screen.
		// This represents the screen area minus the OS taskbar.
		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

		// Save the current window state before maximizing
		state.originalX = stage.getX();
		state.originalY = stage.getY();
		state.originalWidth = stage.getWidth();
		state.originalHeight = stage.getHeight();

		// Maximize the window
		stage.setX(visualBounds.getMinX());
		stage.setY(visualBounds.getMinY());
		stage.setWidth(visualBounds.getWidth());
		stage.setHeight(visualBounds.getHeight());

		state.maximized = true;
	}

	private static void restoreWindow(Stage stage, WindowState state) {
		if (stage == null) return;
		stage.setX(state.originalX);
		stage.setY(state.originalY);
		stage.setWidth(state.originalWidth);
		stage.setHeight(state.originalHeight);

		state.maximized = false;
	}

	/**
	 * Retrieves the WindowState object from the stage's user data.
	 * If one doesn't exist, it creates a new one, attaches it to the stage,
	 * and returns it.
	 */
	private static WindowState getWindowState(Stage stage) {
		Object userData = stage.getUserData();
		if (userData instanceof WindowState) {
			return (WindowState) userData;
		} else {
			WindowState state = new WindowState();
			stage.setUserData(state);
			return state;
		}
	}



}
