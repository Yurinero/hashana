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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserSettings {
	private final Logger logger = LoggerFactory.getLogger(UserSettings.class);

	private static final UserSettings instance = new UserSettings();

	public static class SettingsData {
		public long maxFileSize = 1024;
		public int bufferSize = 64;
		public int progressIntervalMS = 100;
		public boolean splashScreenEnabled = true;
		public String activeTheme = "Dark";
		public int maxEntropyLength = 4096;
	}

	private static final String SETTINGS_FILE = "hashana_settings.json";
	private final Path settingsFilePath;
	private SettingsData currentSettingsData;
	private final ObjectMapper objectMapper;

	private UserSettings() {
		String userHome = System.getProperty("user.home");
		this.settingsFilePath = Paths.get(userHome,".hashana", SETTINGS_FILE);

		this.objectMapper = new ObjectMapper();
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		loadSettings();
	}
	public static UserSettings getInstance() {
		return instance;
	}

	public void loadSettings() {
		File settingsFile = settingsFilePath.toFile();
		if (settingsFile.exists()) {
			try {
				currentSettingsData = objectMapper.readValue(settingsFile, SettingsData.class);
				if (currentSettingsData.activeTheme == null) {
					currentSettingsData.activeTheme = "Dark";
				}
			} catch (IOException e) {
				logger.error("Error while loading settings, going back to Default. Error: {}", e.getMessage());
				currentSettingsData = new SettingsData();
			}
		} else {
			currentSettingsData = new SettingsData();
			saveSettings();
		}
	}

	public void saveSettings() {
		try {
			File parentDir = settingsFilePath.getParent().toFile();
			// If the settings directory doesn't exist, try to create it
			if (!parentDir.exists()) {
				boolean wasCreated = parentDir.mkdirs();
				// If creation failed, log the error and abort
				if (!wasCreated) {
					logger.error("Failed to create parent directories for the settings. Error: {}", parentDir.getAbsolutePath());
					return;
				}
			}
			objectMapper.writeValue(settingsFilePath.toFile(), currentSettingsData);
		} catch (IOException e) {
			logger.error("Error while saving settings, going back to Default. Error: {}", e.getMessage());
		}
	}
	public SettingsData getSettings() {
		return currentSettingsData;
	}
}
