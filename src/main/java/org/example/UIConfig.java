package org.example;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class UIConfig extends Application {

    private final DerivativeConfigState configState = new DerivativeConfigState();
    private final SimulationConfigState simulationState = new SimulationConfigState();
    private final SimpleObjectProperty<DerivativeState>  derivativeState =
            new SimpleObjectProperty<>(new DerivativeState.European());

    public static class SimulationConfigState {
        public SimpleStringProperty timeStepCount;
        public SimpleStringProperty simulationCount;

        public SimulationConfigState() {
            timeStepCount = new SimpleStringProperty("0.01");
            simulationCount = new SimpleStringProperty("1000");
        }

        public StringBinding getBinding() {
            return (StringBinding) Bindings.concat(
                    "{timeStepCount: ", timeStepCount, ", simulationCount: ",  simulationCount, "}");
        }
    }

    public static class DerivativeConfigState {
        public SimpleStringProperty spot;
        public SimpleStringProperty vol;
        public SimpleStringProperty interest;

        public DerivativeConfigState() {
            spot = new SimpleStringProperty("100");
            vol = new SimpleStringProperty("0.25");
            interest = new SimpleStringProperty("0.05");
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

    abstract public static class DerivativeState {

        abstract StringBinding getBinding();

        public static class European extends DerivativeState {
            public SimpleStringProperty isCall;
            public SimpleStringProperty strike;
            public SimpleStringProperty maturity;

            public European() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("120");
                this.maturity = new SimpleStringProperty("1");
            }

            public StringBinding getBinding() {
                return (StringBinding) Bindings.concat(
                        "European{isCall: ", isCall, ", strike: ", strike, ", maturity: ",  maturity, "}");
            }

            @Override
            public String toString() {
                return "European{isCall: " + isCall + ", strike: " + strike + ", maturity: " +  maturity + "}";
            }
        }

        public static class Asian extends DerivativeState {
            public SimpleStringProperty isCall;
            public SimpleStringProperty strike;
            public SimpleStringProperty maturity;

            public Asian() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("120");
                this.maturity = new SimpleStringProperty("1");
            }

            public StringBinding getBinding() {
                return (StringBinding) Bindings.concat(
                        "Asian{isCall: ", isCall, ", strike: ", strike, ", maturity: ",  maturity, "}");
            }

            @Override
            public String toString() {
                return "Asian{isCall: " + isCall + ", strike: " + strike + ", maturity: " +  maturity + "}";
            }
        }

        public static class Barrier extends DerivativeState {
            public SimpleStringProperty barrier;
            public SimpleObjectProperty<DerivativeState> underlying;

            public Barrier() {
                this.barrier = new SimpleStringProperty("120");
                this.underlying = new SimpleObjectProperty<>(new European());
            }

            public StringBinding getBinding() {
                return (StringBinding) Bindings.concat(
                        // Slightly problematic: underlying.get().getBinding() does not update
                        "Barrier{underlying: ", underlying.get().getBinding(), ", barrier: ", barrier, "}");
            }

            @Override
            public String toString() {
                return "Barrier{underlying: " + underlying + ", barrier: " + barrier + "}";
            }
        }
    }

    private Node configBox() {

        Label title = new Label("Derivative configuration");
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

                case "Barrier" -> {
                    derivativeState.set(new DerivativeState.Barrier());
                    yield barrierConfigBox();
                }
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

        DerivativeState.European europeanState;

        if(barrier) {
            DerivativeState.Barrier barrierState = (DerivativeState.Barrier) derivativeState.get();
            barrierState.underlying.set(new DerivativeState.European());
            europeanState = (DerivativeState.European) barrierState.underlying.get();
        } else {
            europeanState = (DerivativeState.European) derivativeState.get();
        }

        strikeField.textProperty().addListener(((_, _, val) -> {
            europeanState.strike.set(val);
            System.out.println(val);
        }));

        maturityField.textProperty().addListener(((_, _, val) -> {
            europeanState.maturity.set(val);
            System.out.println(val);
        }));



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

        DerivativeState.Asian asianState;

        if(barrier) {
            DerivativeState.Barrier barrierState = (DerivativeState.Barrier) derivativeState.get();
            barrierState.underlying.set(new DerivativeState.Asian());
            asianState = (DerivativeState.Asian) barrierState.underlying.get();
        } else {
            asianState = (DerivativeState.Asian) derivativeState.get();
        }

        strikeField.textProperty().addListener(((_, _, val) -> {
            asianState.strike.set(val);
        }));

        maturityField.textProperty().addListener(((_, _, val) -> {
            asianState.maturity.set(val);
        }));



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

        barrierField.textProperty().addListener((_, _, val) -> {
            ((DerivativeState.Barrier) derivativeState.get()).barrier.set(val);
        });

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

    private Node simulationConfigBox() {
        Label title = new Label("Simulation configuration");
        title.setStyle("-fx-font-size: 16px;");

        TextField simulationCountField  = new TextField();
        simulationCountField.setPromptText("Enter simulation count");
        simulationCountField.setMaxWidth(200);
        simulationCountField.textProperty().addListener((_, _, val) -> {
            simulationState.simulationCount.set(val);
        });

        TextField timeStepField  = new TextField();
        timeStepField.setPromptText("Enter time step count");
        timeStepField.setMaxWidth(200);
        timeStepField.textProperty().addListener((_, _, val) -> {
            simulationState.timeStepCount.set(val);
        });


        VBox config = new VBox(8,
                title,
                simulationCountField,
                timeStepField
        );
        config.setPadding(new Insets(10));
        config.setAlignment(Pos.TOP_LEFT);
        return config;
    }

    private Node graphBox() {

        Label title = new Label("Graphs");
        title.setStyle("-fx-font-size: 16px;");


        VBox graphs = new VBox(8.,
                title,
                UIGraph.getSimulationPaths(2, new ArrayList<>()));

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

        Node derivativeConfig = configBox();
        Node simulationConfig = simulationConfigBox();

        Node config = new VBox(8, simulationConfig, derivativeConfig);

        Node graph = graphBox();

        SplitPane sp = new SplitPane(config, graph);

        sp.getDividers().getFirst().positionProperty()
                .addListener((_, _, _) -> {
                    double val = 200.0 / sp.getWidth();
                    sp.setDividerPositions(val);
                });

        Scene scene = new Scene(sp, 1200, 600);


        stage.setTitle("Option pricer");
        stage.setScene(scene);
        stage.show();
        config.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}