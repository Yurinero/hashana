package me.yurinero.hashana.controllers;


import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

	public Label versionInfoLabel;
	public TextArea aboutText;

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
