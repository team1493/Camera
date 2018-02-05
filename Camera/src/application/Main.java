package application;
	
import java.io.FileInputStream;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;


public class Main extends Application {
	   private static Stage primaryStage; // **Declare static Stage**
	   private static Window currentWindow; // **Declare static Stage**
	@Override
	
	public void start(Stage primaryStage)  {
		
		
		try {
	        FXMLLoader loader = new FXMLLoader();
// Path to the FXML File
	        String fxmlDocPath = "C:\\Users\\Teram 1493\\eclipse-workspace\\Camera\\src\\application\\TextAreaExample.fxml";
	        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
	                 
	        AnchorPane root = (AnchorPane) loader.load(fxmlStream);
			Scene scene = new Scene(root,700,700);
			currentWindow=scene.getWindow();
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);  
			primaryStage.show();

			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		launch(args);
	}
	
    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }
    static public Window getWindow() {
        return Main.currentWindow;
    }
	
	
}
