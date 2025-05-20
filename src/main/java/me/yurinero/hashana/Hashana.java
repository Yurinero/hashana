package me.yurinero.hashana;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Hashana extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(Hashana.class.getResource("mainview.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setTitle("Hashana");
		stage.setScene(scene);
		stage.show();

	}

}