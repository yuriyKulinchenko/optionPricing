package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

    private Node configBox() {

        Label title = new Label("Configuration");
        title.setStyle("-fx-font-size: 16px;");

        TextField spotField = new TextField();
        spotField.setPromptText("Enter spot price");
        spotField.setMaxWidth(200);

        TextField volField = new TextField();
        volField.setPromptText("Enter volatility");
        volField.setMaxWidth(200);


        TextField interestField = new TextField();
        interestField.setPromptText("Enter interest rate");
        interestField.setMaxWidth(200);


        StackPane derivativeConfig = new StackPane();
        derivativeConfig.getChildren().add(europeanConfigBox(false));
        derivativeConfig.setMaxWidth(200);

        ComboBox<String> optionSelector = new ComboBox<>();
        optionSelector.setPromptText("Select option type");
        optionSelector.getItems().addAll("European", "Asian", "Barrier");
        optionSelector.setValue("European");
        optionSelector.setMaxWidth(200);


        optionSelector.setOnAction(e -> {
            String selected = optionSelector.getValue();
            derivativeConfig.getChildren().setAll(switch (selected)  {
                case "European" -> europeanConfigBox(false);
                case "Asian" -> asianConfigBox(false);
                case "Barrier" -> barrierConfigBox();
                default -> throw new IllegalStateException("Unexpected value: " + selected);
            });
        });


        Button btn = new Button("Calculate option price");

        btn.setMaxWidth(200);

        VBox config = new VBox(8.,
                title,
                spotField,
                volField,
                interestField,
                optionSelector,
                getSeparator(Orientation.HORIZONTAL),
                derivativeConfig,
                getSeparator(Orientation.HORIZONTAL),
                btn
        );

        config.setPadding(new Insets(10));
        config.setAlignment(Pos.TOP_LEFT);
        return config;
    }

    private Node europeanConfigBox(boolean barrier) {
        TextField spotField = new TextField();
        spotField.setPromptText("Enter spot price");
        spotField.setMaxWidth(200);

        return new VBox(8,
                new Label("European option" + (barrier ? " (underlying)" : "")),
                spotField
                );
    }

    private Node asianConfigBox(boolean barrier) {
        TextField spotField = new TextField();
        spotField.setPromptText("Enter spot price");
        spotField.setMaxWidth(200);

        return new VBox(8,
                new Label("Asian option" + (barrier ? " (underlying)" : "")),
                spotField
        );
    }

    private Node barrierConfigBox() {
        TextField barrierField = new TextField();
        barrierField.setPromptText("Enter barrier price");
        barrierField.setMaxWidth(200);

        StackPane derivativeConfig = new StackPane();
        derivativeConfig.getChildren().add(europeanConfigBox(true));

        ComboBox<String> optionSelector = new ComboBox<>();
        optionSelector.setPromptText("Select option type");
        optionSelector.getItems().addAll("European", "Asian");
        optionSelector.setValue("European");
        optionSelector.setMaxWidth(200);

        optionSelector.setOnAction(e -> {
            String selected = optionSelector.getValue();
            derivativeConfig.getChildren().setAll(switch (selected)  {
                case "European" -> europeanConfigBox(true);
                case "Asian" -> asianConfigBox(true);
                default -> throw new IllegalStateException("Unexpected value: " + selected);
            });
        });

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);

        return new VBox(8,
                new Label("Barrier option"),
                barrierField,
                optionSelector,
                separator,
                derivativeConfig
                );
    }

    private Node graphBox() {

        Label title = new Label("Graphs");
        title.setStyle("-fx-font-size: 16px;");


        VBox graphs = new VBox(8.,
                title
        );

        graphs.setPadding(new Insets(10));
        graphs.setAlignment(Pos.TOP_LEFT);
        return graphs;
    }

    private Separator getSeparator(Orientation orientation) {
        Separator separator = new Separator();
        separator.setOrientation(orientation);
        return separator;
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
        StackPane root = new StackPane(box); // Temp

        SplitPane sp = new SplitPane(configBox(), graphBox());

        sp.getDividers().getFirst().positionProperty()
                .addListener((_, _, _) -> {
                    double val = 200.0 / sp.getWidth();
                    sp.setDividerPositions(val);
                });

        Scene scene = new Scene(sp, 1000, 500);


        stage.setTitle("Hello JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}