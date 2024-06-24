package io.github.jagodevreede.sdkmanui;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkmanui.service.GlobalExceptionHandler;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws Exception {
        logger.debug("Starting SDKMAN UI");
        loadServiceRegistry();
        Parameters params = getParameters();
        List<String> list = params.getRaw();
        for (String each : list) {
            System.out.println(each);
        }

        setApplicationIconImage(stage);
        if (!ConfigurationUtil.preCheck(stage)) {
            logger.warn("Failed pre-check");
            return;
        }
        ServiceRegistry.INSTANCE.getApi().registerShutdownHook();

        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        URL mainFxml = Main.class.getClassLoader().getResource("main.fxml");
        Parent root = FXMLLoader.load(mainFxml);

        Scene scene = new Scene(root, 800, 580);
        stage.setResizable(false);

        stage.setTitle("SDKMAN UI");
        stage.setScene(scene);
        stage.show();
    }

    private void loadServiceRegistry() {
        Thread loaderThread = new Thread(() -> {
            // Load the preferences, then everything is ready to go
            ServiceRegistry.INSTANCE.getSdkManUiPreferences();
        });
        loaderThread.setDaemon(true);
        loaderThread.setName("ServiceRegistry loader");
        loaderThread.start();
    }

    private void setApplicationIconImage(Stage stage) {
        if (!OsHelper.isMac()) {
            // Only for mac other os are not needed
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sdkman_ui_logo.png")));
            stage.getIcons().add(appIcon);
            return;
        }
        Thread loaderThread = new Thread(() -> {
            final java.awt.Toolkit defaultToolkit = java.awt.Toolkit.getDefaultToolkit();
            final URL imageResource = getClass().getResource("/images/sdkman_ui_logo.png");
            final java.awt.Image image = defaultToolkit.getImage(imageResource);

            final java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();

            try {
                //set icon for mac os (and other systems which do support this method)
                taskbar.setIconImage(image);
            } catch (final UnsupportedOperationException e) {
                logger.debug("The os does not support: 'taskbar.setIconImage'");
            } catch (final SecurityException e) {
                logger.debug("There was a security exception for: 'taskbar.setIconImage'");
            }
        });
        loaderThread.setDaemon(true);
        loaderThread.setName("Osx dock icon loader");
        loaderThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}