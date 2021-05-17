package install;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.stage.Stage;

import logging.LoggerWrapper;
import researchApp.GlobalVariables;
import java.io.IOException;

public class Splash {
    private Stage splashStage;

    public Splash(Stage stg){
        splashStage = stg;
        new SplashScreen().start();
    }

    class SplashScreen extends Thread{
        public void run(){
            try {
                Thread.sleep(3500);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage stage = new Stage();
                        try{
                            Parent root = FXMLLoader.load(getClass().getResource("/researchApp/research.fxml"));
                            Scene scene = new Scene(root);
                            stage.getIcons().add(new Image("images/icon.png"));
                            stage.setScene(scene);
                            stage.setTitle(GlobalVariables.TITLE);
                            stage.setResizable(false);
                            stage.show();

                        }catch(IOException | NullPointerException ex){
                            System.out.println(ex.toString());
                            LoggerWrapper.getInstance().myLogger.severe("SplashScreen failure: Launching ResearchController.");
                        }
                        splashStage.getScene().getWindow().hide();
                    }
                });

            }catch(InterruptedException ie){
                LoggerWrapper.getInstance().myLogger.severe("SplashScreen sleep failed.");
            }

        }
    }
}


