package me.yurinero.hashana.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserSettings {

	public static class SettingsData {
		public long maxFileSize = 1024;
		public int bufferSize = 64;
		public int progressIntervalMS = 100;
		public boolean splashScreenEnabled = true;
	}

	private static final String SETTINGS_FILE = "hashana_settings.json";
	private Path settingsFilePath;
	private SettingsData currentSettingsData;
	private ObjectMapper objectMapper;

	public UserSettings() {
		String userHome = System.getProperty("user.home");
		this.settingsFilePath = Paths.get(userHome,".hashana", SETTINGS_FILE);

		this.objectMapper = new ObjectMapper();
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		loadSettings();
	}

	public void loadSettings() {
		File settingsFile = settingsFilePath.toFile();
		if (settingsFile.exists()) {
			try {
				currentSettingsData = objectMapper.readValue(settingsFile, SettingsData.class);
			} catch (IOException e) {
				System.err.println("Error loading settings, using defaults: " + e.getMessage());
			}
		} else {
			currentSettingsData = new SettingsData();
			saveSettings();
		}
	}

	private void saveSettings() {
		try {
			File parentDir = settingsFilePath.getParent().toFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			objectMapper.writeValue(settingsFilePath.toFile(), currentSettingsData);
		} catch (IOException e) {
			System.err.println("Error saving settings, using defaults: " + e.getMessage());
		}
	}
	public SettingsData getSettings() {
		return currentSettingsData;
	}
}
