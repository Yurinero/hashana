package me.yurinero.hashana.utils;

public class ThemeUtils {
	private ThemeUtils() {

	}
	public static String getCssPathForTheme(String themeName) {
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
}