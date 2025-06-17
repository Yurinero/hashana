package me.yurinero.hashana.utils;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * A utility class to make an undecorated JavaFX Stage resizable by dragging its edges/corners.
 */
public class WindowResizer {

	// Class Fields

	private final Stage stage;
	private final Scene scene;

	// The pixel margin from the edge of the window where resizing is possible.
	private static final int RESIZE_MARGIN = 8;

	// Variables to store the state when a resize operation begins.
	private double xOffset = 0; // The mouse's screen X position at the start of the drag.
	private double yOffset = 0; // The mouse's screen Y position at the start of the drag.
	private double startWidth = 0; // The window's width at the start of the drag.
	private double startHeight = 0; // The window's height at the start of the drag.
	private double startX = 0; // The window's X position at the start of the drag.
	private double startY = 0; // The window's Y position at the start of the drag.

	// The current cursor type, determined by the mouse's position.
	private Cursor cursor = Cursor.DEFAULT;

	/**
	 * Constructor that takes the stage to make resizable.
	 * @param stage The undecorated Stage.
	 */
	public WindowResizer(Stage stage) {
		this.stage = stage;
		this.scene = stage.getScene();

		// We use addEventFilter instead of setOn... to ensure our logic runs
		// during the "event capturing" phase. This means we get the event
		// *before* any child nodes (like a TabPane) can consume it.
		scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
	}

	/**
	 * Called continuously as the mouse moves over the scene.
	 * Its job is to determine what the cursor should look like.
	 */
	private void handleMouseMoved(MouseEvent event) {
		// Determine the new cursor type based on the mouse's current position.
		cursor = getCursorForPosition(event.getSceneX(), event.getSceneY(), scene.getWidth(), scene.getHeight());
		// Set the scene's cursor.
		scene.setCursor(cursor);
	}

	/**
	 * Called when the user presses a mouse button.
	 * If the cursor is a resize cursor, it captures the initial state of the window and mouse.
	 */
	private void handleMousePressed(MouseEvent event) {
		// Only start a resize operation if the cursor is not the default arrow.
		if (cursor != Cursor.DEFAULT) {
			// Record the starting state.
			startX = stage.getX();
			startY = stage.getY();
			startWidth = stage.getWidth();
			startHeight = stage.getHeight();
			// Record the mouse's absolute screen position.
			xOffset = event.getScreenX();
			yOffset = event.getScreenY();
		}
	}

	/**
	 * Called when the user drags the mouse with the button held down.
	 * If a resize is in progress, it calculates and applies the new window dimensions.
	 */
	private void handleMouseDragged(MouseEvent event) {
		// Only perform resizing if the drag started in a resize zone.
		if (cursor != Cursor.DEFAULT) {
			// Calculate the change (delta) in the mouse's position since the drag started.
			double deltaX = event.getScreenX() - xOffset;
			double deltaY = event.getScreenY() - yOffset;

			// Pass the deltas to the resize handler.
			handleResize(deltaX, deltaY);
		}
	}

	/**
	 * Determines which resize cursor to display based on the mouse's (x,y) coordinates.
	 */
	private Cursor getCursorForPosition(double x, double y, double width, double height) {
		boolean onTop = y >= 0 && y < RESIZE_MARGIN;
		boolean onLeft = x >= 0 && x < RESIZE_MARGIN;
		boolean onRight = x <= width && x > width - RESIZE_MARGIN;
		boolean onBottom = y <= height && y > height - RESIZE_MARGIN;

		// Check corners first, as they are more specific than sides.
		if (onTop && onLeft) return Cursor.NW_RESIZE;
		if (onTop && onRight) return Cursor.NE_RESIZE;
		if (onBottom && onLeft) return Cursor.SW_RESIZE;
		if (onBottom && onRight) return Cursor.SE_RESIZE;
		// Check sides.
		if (onTop) return Cursor.N_RESIZE;
		if (onLeft) return Cursor.W_RESIZE;
		if (onRight) return Cursor.E_RESIZE;
		if (onBottom) return Cursor.S_RESIZE;

		// If not near any edge, return the default cursor.
		return Cursor.DEFAULT;
	}

	/**
	 * Calculates and applies the new size and position of the stage based on mouse deltas.
	 */
	private void handleResize(double deltaX, double deltaY) {
		// Horizontal Resizing
		// If dragging the left, top-left, or bottom-left edge...
		if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
			// The new width is the starting width minus the horizontal mouse movement.
			double newWidth = startWidth - deltaX;
			// Ensure the window doesn't get smaller than its minimum allowed width.
			if (newWidth >= stage.getMinWidth()) {
				// Move the window's X position and set the new width.
				stage.setX(startX + deltaX);
				stage.setWidth(newWidth);
			}
		}
		// If dragging the right, top-right, or bottom-right edge...
		else if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
			// The new width is the starting width plus the horizontal mouse movement.
			double newWidth = startWidth + deltaX;
			if (newWidth >= stage.getMinWidth()) {
				stage.setWidth(newWidth);
			}
		}

		// Vertical Resizing
		// If dragging the top, top-left, or top-right edge...
		if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
			double newHeight = startHeight - deltaY;
			if (newHeight >= stage.getMinHeight()) {
				stage.setY(startY + deltaY);
				stage.setHeight(newHeight);
			}
		}
		// If dragging the bottom, bottom-left, or bottom-right edge...
		else if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
			double newHeight = startHeight + deltaY;
			if (newHeight >= stage.getMinHeight()) {
				stage.setHeight(newHeight);
			}
		}
	}
}