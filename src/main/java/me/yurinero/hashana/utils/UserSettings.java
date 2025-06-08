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
	private Path settingsFilePath;
	private SettingsData currentSettingsData;
	private ObjectMapper objectMapper;

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
			if (!parentDir.exists()) {
				parentDir.mkdirs();
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
