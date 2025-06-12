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

package me.yurinero.hashana.controllers;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

	public Label versionInfoLabel;
	public TextArea aboutText;
	private  static final Logger logger = LoggerFactory.getLogger(AboutController.class);
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		Properties prop = new Properties();
		try (InputStream inputStream = getClass().getResourceAsStream("/me/yurinero/hashana/version.properties")) {
			if (inputStream != null) {
				prop.load(inputStream);
				String version = prop.getProperty("app.version", "N/A");
				versionInfoLabel.setText(version);
			} else {
				versionInfoLabel.setText("Not found");
			}
		} catch (IOException e) {
			versionInfoLabel.setText("Error");

		}
		aboutText.setText(
				"""	
						
						Hashana is a desktop utility designed for hashing text and files, generating secure passwords, and verifying file integrity with checksums.
						
						This application was created as a way to learn working with JavaFX and creating a fun small application while doing so.
						
						Thank you for checking it out!
						
						--- Credits ---
						This application is proudly built with these fantastic open-source libraries:
						
						• JavaFX: openjfx.io
						• Google Guava: github.com/google/guava
						• Jackson: github.com/FasterXML/jackson
						• SLF4J & Logback: www.slf4j.org & logback.qos.ch
						
						--- Disclaimer ---
						
						THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
						INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
						IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
						WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."""
		);
	}

}
