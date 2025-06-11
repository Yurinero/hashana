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