package org.example;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleDoubleProperty;
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

    private final ConfigState configState = new ConfigState();
    private final SimpleObjectProperty<DerivativeState>  derivativeState =
            new SimpleObjectProperty<>(new DerivativeState.European());

    private static class ConfigState {
        public SimpleStringProperty spot = new SimpleStringProperty();
        public SimpleStringProperty vol = new SimpleStringProperty();
        public SimpleStringProperty interest = new SimpleStringProperty();

        public ConfigState() {
            spot.set("100");
            vol.set("0.25");
            interest.set("0.05");
        }

        public ConfigState(double spot, double vol, double interest) {
            this.spot.set(Double.toString(spot));
            this.vol.set(Double.toString(vol));
            this.interest.set(Double.toString(interest));
        }

        @Override
        public String toString() {
            return "{spot: " + spot + ", vol: " + vol + ", interest: " + interest + "}";
        }

        public StringBinding getBinding() {
            return (StringBinding) Bindings.concat(
                    "{spot: ", spot, ", vol: ",  vol, ", interest: ",  interest, "}");
        }
    }

    abstract private static class DerivativeState {

        abstract StringBinding getBinding();

        public static class European extends DerivativeState {
            public SimpleStringProperty isCall = new SimpleStringProperty();
            public SimpleStringProperty strike = new SimpleStringProperty();
            public SimpleStringProperty maturity = new SimpleStringProperty();

            public European() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("1.2");
                this.maturity = new SimpleStringProperty("1");
            }

            public European(boolean isCall, double strike, double maturity) {
                this.isCall.set(Boolean.toString(isCall));
                this.strike.set(Double.toString(strike));
                this.maturity.set(Double.toString(maturity));
            }

            public StringBinding getBinding() {
                return (StringBinding) Bindings.concat(
                        "European{isCall: ", isCall, ", strike: ", strike, ", maturity: ",  maturity, "}");
            }

            @Override
            public String toString() {
                return "European{" +
                        "isCall=" + isCall +
                        ", strike=" + strike +
                        ", maturity=" + maturity +
                        '}';
            }
        }

        public static class Asian extends DerivativeState {
            public SimpleStringProperty isCall = new SimpleStringProperty();
            public SimpleStringProperty strike = new SimpleStringProperty();
            public SimpleStringProperty maturity = new SimpleStringProperty();

            public Asian() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("1.2");
                this.maturity = new SimpleStringProperty("1");
            }

            public Asian(boolean isCall, double strike, double maturity) {
                this.isCall.set(Boolean.toString(isCall));
                this.strike.set(Double.toString(strike));
                this.maturity.set(Double.toString(maturity));
            }

            public StringBinding getBinding() {
                return (StringBinding) Bindings.concat(
                        "Asian{isCall: ", isCall, ", strike: ", strike, ", maturity: ",  maturity, "}");
            }

            @Override
            public String toString() {
                return "Asian{" +
                        "isCall=" + isCall +
                        ", strike=" + strike +
                        ", maturity=" + maturity +
                        '}';
            }
        }
    }

    private Node configBox() {

        Label title = new Label("Configuration");
        title.setStyle("-fx-font-size: 16px;");

        TextField spotField = new TextField();
        spotField.setPromptText("Enter spot price");
        spotField.setMaxWidth(200);
        spotField.textProperty().addListener((obs, oldVal, newVal) -> {
            configState.spot.set(newVal);
        });

        TextField volField = new TextField();
        volField.setPromptText("Enter volatility");
        volField.setMaxWidth(200);
        volField.textProperty().addListener((obs, oldVal, newVal) -> {
            configState.vol.set(newVal);
        });


        TextField interestField = new TextField();
        interestField.setPromptText("Enter interest rate");
        interestField.setMaxWidth(200);
        interestField.textProperty().addListener((obs, oldVal, newVal) -> {
            configState.interest.set(newVal);
        });


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
                case "European" -> {
                    derivativeState.set(new DerivativeState.European());
                    yield europeanConfigBox(false);
                }

                case "Asian" -> {
                    derivativeState.set(new DerivativeState.Asian());
                    yield asianConfigBox(false);
                }

                case "Barrier" -> barrierConfigBox();
                default -> throw new IllegalStateException("Unexpected value: " + selected);
            });
        });


        Button btn = new Button("Calculate option price");

        btn.setOnAction(e -> {

        }); // Nothing for now

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
        TextField strikeField = new TextField();
        strikeField.setPromptText("Enter strike price");
        strikeField.setMaxWidth(200);

        TextField maturityField = new TextField();
        maturityField.setPromptText("Enter maturity");
        maturityField.setMaxWidth(200);

        if(barrier) {
            // Nothing for now
        } else {
            strikeField.textProperty().addListener(((obs, oldVal, newVal) -> {
                ((DerivativeState.European) derivativeState.get()).strike.set(newVal);
            }));

            maturityField.textProperty().addListener(((obs, oldVal, newVal) -> {
                ((DerivativeState.European) derivativeState.get()).maturity.set(newVal);
            }));
        }

        return new VBox(8,
                new Label("European option" + (barrier ? " (underlying)" : "")),
                strikeField,
                maturityField
                );
    }

    private Node asianConfigBox(boolean barrier) {
        TextField strikeField = new TextField();
        strikeField.setPromptText("Enter strike price");
        strikeField.setMaxWidth(200);

        TextField maturityField = new TextField();
        maturityField.setPromptText("Enter maturity");
        maturityField.setMaxWidth(200);

        if(barrier) {
            // Nothing for now
        } else {
            strikeField.textProperty().addListener(((obs, oldVal, newVal) -> {
                ((DerivativeState.Asian) derivativeState.get()).strike.set(newVal);
            }));

            maturityField.textProperty().addListener(((obs, oldVal, newVal) -> {
                ((DerivativeState.Asian) derivativeState.get()).maturity.set(newVal);
            }));
        }

        return new VBox(8,
                new Label("Asian option" + (barrier ? " (underlying)" : "")),
                strikeField,
                maturityField
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

        // Temporarily display config state:

        Label config = new Label();
        config.textProperty().bind(configState.getBinding());

        Label derivative = new Label();
        derivative.textProperty().bind(derivativeState.getValue().getBinding());
        derivativeState.addListener((obs, oldVal, newVal) -> {
            derivative.textProperty().bind(newVal.getBinding());
        });


        VBox graphs = new VBox(8.,
                title,
                config,
                derivative
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

        Node config = configBox();
        Node graph = graphBox();

        SplitPane sp = new SplitPane(config, graph);

        sp.getDividers().getFirst().positionProperty()
                .addListener((_, _, _) -> {
                    double val = 200.0 / sp.getWidth();
                    sp.setDividerPositions(val);
                });

        Scene scene = new Scene(sp, 1000, 500);


        stage.setTitle("Option pricer");
        stage.setScene(scene);
        stage.show();
        config.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}