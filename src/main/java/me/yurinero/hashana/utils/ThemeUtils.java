package me.yurinero.hashana.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThemeUtils {
	private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);

	private ThemeUtils() {

	}
	public static String getCssPathForTheme(String themeName) {
		return switch (themeName) {
			case "Light" -> "/me/yurinero/hashana/light-theme.css";
			case "Accessible" -> "/me/yurinero/hashana/accessible-theme.css";
			case "Dark" -> "/me/yurinero/hashana/dark-theme.css"; // Default/fallback
			default -> {
				logger.error("Theme could not be found, defaulting to theme Dark: {}", themeName);
				yield "/me/yurinero/hashana/dark-theme.css";
			}
		};
	}
}