package io.nbrs.dynamicql;

import io.nbrs.dynamicql.controller.MainViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by Antonio Zaitoun on 2020-01-04.
 */
public class AppDelegate extends Application {

    MainViewController controller = new MainViewController();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(AppDelegate.class.getResourceAsStream("/logo.png")));

        primaryStage.setTitle("DynamicQL Studio");
        primaryStage.setScene(new Scene(controller.view));
        primaryStage.show();
        primaryStage.setOnCloseRequest((ae) -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String... args) {
        AppDelegate.launch(args);
    }
}
