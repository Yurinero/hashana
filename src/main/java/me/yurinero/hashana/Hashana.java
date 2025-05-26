package me.yurinero.hashana;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.yurinero.hashana.controllers.MainViewController;
import me.yurinero.hashana.utils.SplashScreen;
import me.yurinero.hashana.utils.ThreadPoolService;

import java.io.IOException;


public class Hashana extends Application {

	@Override
	public void init() throws Exception {
		/* Sleeping the thread on purpose upon initialization of the application to display the splash screen.
		* This is absolutely fucking stupid and only done for aesthetics.*/
		Thread.sleep(2000);
	}

	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(Hashana.class.getResource("main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		// Needed to pass the stage to the MainViewController
		MainViewController controller = fxmlLoader.getController();
		controller.setStage(stage);

		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("Hashana");
		stage.setScene(scene);
		stage.show();



	}

	// Calls the shutdown method from the ThreadPoolService to shut down any tasks running on background threads.
	public void stop(){
		ThreadPoolService.getInstance().shutdown();
	}
	public static void main(String[] args) {
		System.setProperty("javafx.preloader", SplashScreen.class.getName());
		Application.launch(Hashana.class, args);
	}
}