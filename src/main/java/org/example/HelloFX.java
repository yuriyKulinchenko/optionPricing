package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

public class HelloFX extends Application {

    private SimpleObjectProperty<ConfigState> configState =
            new SimpleObjectProperty<>(new ConfigState());
    private SimpleObjectProperty<DerivativeState> derivativeState =
            new SimpleObjectProperty<>(new DerivativeState());

    private static class ConfigState {
        public double spot;
        public double vol;
        public double interest;

        public ConfigState() {
            this.spot = 100;
            this.vol = 0.25;
            this.interest = 0.05;
        }

        public ConfigState(double spot, double vol, double interest) {
            this.spot = spot;
            this.vol = vol;
            this.interest = interest;
        }
    }

    private static class DerivativeState {}

    private VBox configBox() {

        TextField spotField = new TextField();
        spotField.setPromptText("Enter spot price");

        TextField volField = new TextField();
        volField.setPromptText("Enter volatility");

        TextField interestField = new TextField();
        interestField.setPromptText("Enter interest rate");

        StackPane derivativeConfig = new StackPane();
        derivativeConfig.getChildren().add(europeanConfigBox());

        ComboBox<String> optionSelector = new ComboBox<>();
        optionSelector.setPromptText("Select option type");
        optionSelector.getItems().addAll("European", "Asian", "Barrier");
        optionSelector.setValue("European");


        optionSelector.setOnAction(e -> {
            String selected = optionSelector.getValue();
            derivativeConfig.getChildren().setAll(switch (selected)  {
                case "European" -> europeanConfigBox();
                case "Asian" -> asianConfigBox();
                case "Barrier" -> barrierConfigBox();
                default -> throw new IllegalStateException("Unexpected value: " + selected);
            });
        });

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);

        return new VBox(8.,
                spotField,
                volField,
                interestField,
                optionSelector,
                separator,
                derivativeConfig
        );
    }

    private VBox europeanConfigBox() {
        Label label = new Label("European option configuration");
        return new VBox(8, label);
    }

    private VBox asianConfigBox() {
        Label label = new Label("Asian option configuration");
        return new VBox(8, label);
    }

    private VBox barrierConfigBox() {
        Label label = new Label("Barrier option configuration");
        return new VBox(8, label);
    }


    @Override
    public void start(Stage stage) {

        SimpleStringProperty property = new SimpleStringProperty("");

        // Text field

        TextField inputField = new TextField();
        inputField.setPromptText("Test input");

        // Button

        Button btn = new Button("Click me");
        btn.setOnAction(e -> {
            property.set(inputField.textProperty().get());
        });

        // Text output

        Label mirrorLabel = new Label();
        mirrorLabel.textProperty().bind(property);

        // Assembly

        VBox box = new VBox(20, inputField, btn, mirrorLabel);

        StackPane root = new StackPane(box);

        SplitPane sp = new SplitPane(configBox(), root);

        Scene scene = new Scene(sp, 400, 300);


        stage.setTitle("Hello JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}