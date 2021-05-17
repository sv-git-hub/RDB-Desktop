package researchApp;

import install.Install;
import install.Splash;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logging.LoggerWrapper;

public class ResearchApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception{
        new Splash(stage);
        LoggerWrapper.getInstance().myLogger.finest("Begin install...");
        LoggerWrapper.getInstance().myLogger.config("Splash splash launched...");
        new Install();
        LoggerWrapper.getInstance().myLogger.finest("Begin research.fxml...");

        Parent root = FXMLLoader.load(getClass().getResource("/install/splash.fxml"));
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("images/icon.png"));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
    }

}
