package io.github.jagodevreede.sdkmanui.view;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.domain.JavaVersion;
import io.github.jagodevreede.sdkmanui.MainScreenController;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Optional;

public class JavaVersionView {

    private final SimpleStringProperty vendor;
    private final SimpleStringProperty version;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty identifier;
    private final HBox actions;
    private final CheckBox installed;
    private final CheckBox available;
    private final Button globalAction;
    private final Button useAction;
    private final MainScreenController controller;

    public JavaVersionView(JavaVersion javaVersion, String javaGlobalVersionInUse, String javaPathVersionInUse, MainScreenController controller) {
        this.controller = controller;
        globalAction = createImageButton("/images/global_icon.png", javaVersion.identifier().equals(javaGlobalVersionInUse), globalEventHandler(javaVersion));
        useAction = createImageButton("/images/use_icon.png", javaVersion.identifier().equals(javaPathVersionInUse), useEventHandler(javaVersion));
        this.vendor = new SimpleStringProperty(javaVersion.vendor());
        this.version = new SimpleStringProperty(javaVersion.version());
        this.dist = new SimpleStringProperty(javaVersion.dist());
        this.identifier = new SimpleStringProperty(javaVersion.identifier());
        if (javaVersion.installed()) {
            this.actions = new HBox(
                    globalAction,
                    useAction
            );
        } else {
            this.actions = new HBox();
        }
        this.installed = new CheckBox();
        this.installed.setSelected(javaVersion.installed());
        this.installed.selectedProperty().addListener(installedChangeListener(javaVersion));
        this.available = new CheckBox();
        this.available.setDisable(true);
        this.available.setSelected(javaVersion.available());
    }

    private ChangeListener<Boolean> installedChangeListener(JavaVersion javaVersion) {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                controller.downloadAndInstall("java", javaVersion.identifier());
            } else {
                controller.uninstall("java", javaVersion.identifier());
            }
        };
    }

    private EventHandler<MouseEvent> globalEventHandler(JavaVersion javaVersion) {
        return (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set global SDK");
            alert.setHeaderText("Are you sure that you want to set " + javaVersion.identifier() + " as your global SDK?");

            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType buttonYesAndClose = new ButtonType("Yes, and close", ButtonBar.ButtonData.YES);

            alert.getButtonTypes().setAll(buttonTypeCancel, buttonYes, buttonYesAndClose);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == buttonYesAndClose || result.get() == buttonYes)) {
                SdkManApi api = ServiceRegistry.INSTANCE.getApi();
                try {
                    api.changeGlobal("java", javaVersion.identifier());
                } catch (IOException e) {
                    ServiceRegistry.INSTANCE.getPopupView().showError(e);
                }
                if (result.get() == buttonYesAndClose) {
                    Platform.exit();
                } else {
                    controller.loadData();
                }
            }
            alert.close();
        };
    }

    private EventHandler<MouseEvent> useEventHandler(JavaVersion javaVersion) {
        return (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set local SDK");
            alert.setHeaderText("Are you sure that you want to set " + javaVersion.identifier() + " as your local SDK?");

            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType buttonYesAndClose = new ButtonType("Yes, and close", ButtonBar.ButtonData.YES);

            alert.getButtonTypes().setAll(buttonTypeCancel, buttonYes, buttonYesAndClose);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == buttonYesAndClose || result.get() == buttonYes)) {
                SdkManApi api = ServiceRegistry.INSTANCE.getApi();
                try {
                    api.createExitScript("java", javaVersion.identifier());
                } catch (IOException e) {
                    ServiceRegistry.INSTANCE.getPopupView().showError(e);
                }
                if (result.get() == buttonYesAndClose) {
                    Platform.exit();
                } else {
                    controller.loadData();
                }
            }
            alert.close();
        };
    }

    private Button createImageButton(String imagePath, boolean disabled, EventHandler<? super MouseEvent> eventHandler) {
        Button button = new Button();
        button.setDisable(disabled);
        ImageView globalActionImage = new ImageView();
        globalActionImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        globalActionImage.setFitHeight(10.0);
        globalActionImage.setFitWidth(10.0);
        button.setGraphic(globalActionImage);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(10.0);
        button.setMaxHeight(10.0);
        button.setOnMouseClicked(eventHandler);
        return button;
    }

    public String getVendor() {
        return vendor.get();
    }

    public String getVersion() {
        return version.get();
    }

    public String getDist() {
        return dist.get();
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public CheckBox getInstalled() {
        return installed;
    }

    public CheckBox getAvailable() {
        return available;
    }

    public HBox getActions() {
        return actions;
    }

}
