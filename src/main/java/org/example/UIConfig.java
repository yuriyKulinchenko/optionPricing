package org.example;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.function.Supplier;

public class UIConfig extends Application {

    private final DerivativeConfigState configState = new DerivativeConfigState();
    private final SimulationConfigState simulationState = new SimulationConfigState();

    private final SimpleObjectProperty<DerivativeState>  derivativeState =
            new SimpleObjectProperty<>(new DerivativeState.European());
    private final SimpleObjectProperty<DerivativePricer.PricerResult> pricerResult =
            new SimpleObjectProperty<>();

    public static class SimulationConfigState {
        public SimpleStringProperty timeStepCount;
        public SimpleStringProperty simulationCount;

        public SimulationConfigState() {
            timeStepCount = new SimpleStringProperty("100");
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

    private TextField createConfigField(SimpleStringProperty property) {
        TextField field = new TextField();
        field.setMaxWidth(100);
        field.setText(property.get());
        field.textProperty().addListener((_, _, val) -> property.set(val));
        return field;
    }

    private GridPane createConfigForm() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setHgrow(Priority.NEVER);

        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setHgrow(Priority.ALWAYS);
        rightCol.setHalignment(HPos.RIGHT);

        form.getColumnConstraints().addAll(leftCol, rightCol);
        return form;
    }

    private Node configBox() {

        Label title = new Label("Derivative configuration");
        title.setStyle("-fx-font-size: 16px;");

        GridPane form = createConfigForm();

        form.addRow(0, new Label("Underlying Price"), createConfigField(configState.spot));
        form.addRow(1, new Label("Volatility"), createConfigField(configState.vol));
        form.addRow(2, new Label("Interest Rates"), createConfigField(configState.interest));


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

            // Parse simulation state:

            int simulationCount = Integer.parseInt(simulationState.simulationCount.get());
            int stepCount = Integer.parseInt(simulationState.timeStepCount.get());

            DerivativePricer pricer = new MonteCarloPricer.Builder()
                    .setIterationCount(simulationCount)
                    .setSteps(stepCount)
                    .build();

            // Parse config state:

            double spot = Double.parseDouble(configState.spot.get());
            double interest = Double.parseDouble(configState.interest.get());
            double vol = Double.parseDouble(configState.vol.get());

            Supplier<StochasticProcess> supplier = () -> new GeometricBrownianMotion(spot, interest, vol);

            // Parse derivative:

            Derivative derivative = null;

            DerivativeState state = derivativeState.get();

            if(state instanceof DerivativeState.European european) {
                double strike = Double.parseDouble(european.strike.get());
                double maturity = Double.parseDouble(european.maturity.get());
                derivative = new EuropeanCall(strike, maturity);
            } else if(state instanceof DerivativeState.Asian asian) {
                double strike = Double.parseDouble(asian.strike.get());
                double maturity = Double.parseDouble(asian.maturity.get());
                derivative = new AsianCall(strike, maturity);
            } else if(state instanceof DerivativeState.Barrier barrier) {
                // No handling for now
            }
            pricerResult.set(pricer.getPrice(derivative, supplier));
        });

        btn.setMaxWidth(200);

        VBox config = new VBox(8.,
                title,
                form,
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

        DerivativeState.European europeanState;

        if(barrier) {
            DerivativeState.Barrier barrierState = (DerivativeState.Barrier) derivativeState.get();
            barrierState.underlying.set(new DerivativeState.European());
            europeanState = (DerivativeState.European) barrierState.underlying.get();
        } else {
            europeanState = (DerivativeState.European) derivativeState.get();
        }

        GridPane form = createConfigForm();

        form.addRow(0, new Label("Strike price"), createConfigField(europeanState.strike));
        form.addRow(1, new Label("Maturity"), createConfigField(europeanState.maturity));

        return new VBox(8,
                new Label("European option" + (barrier ? " (underlying)" : "")), form);
    }

    private Node asianConfigBox(boolean barrier) {

        DerivativeState.Asian asianState;

        if(barrier) {
            DerivativeState.Barrier barrierState = (DerivativeState.Barrier) derivativeState.get();
            barrierState.underlying.set(new DerivativeState.Asian());
            asianState = (DerivativeState.Asian) barrierState.underlying.get();
        } else {
            asianState = (DerivativeState.Asian) derivativeState.get();
        }

        GridPane form = createConfigForm();

        form.addRow(0, new Label("Strike price"), createConfigField(asianState.strike));
        form.addRow(1, new Label("Maturity"), createConfigField(asianState.maturity));

        return new VBox(8, new Label("Asian option" + (barrier ? " (underlying)" : "")), form);
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

        GridPane form = createConfigForm();

        form.addRow(0, new Label("Path count"), createConfigField(simulationState.simulationCount));
        form.addRow(1, new Label("Time steps"), createConfigField(simulationState.timeStepCount));


        VBox config = new VBox(8, title, form);
        config.setPadding(new Insets(10));
        config.setAlignment(Pos.TOP_LEFT);
        return config;
    }

    private Node graphBox() {

        Label title = new Label("Graphs");
        title.setStyle("-fx-font-size: 16px;");

        VBox simulationGraph = new VBox(8, UIGraph.getSimulationPaths(new ArrayList<>()));

        pricerResult.addListener((_, _, val) -> {
            simulationGraph.getChildren().removeFirst();
            simulationGraph.getChildren().addAll(UIGraph.getSimulationPaths(val.paths));
        });


        VBox graphs = new VBox(8.,
                title,
                simulationGraph
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

        Node derivativeConfig = configBox();
        Node simulationConfig = simulationConfigBox();

        Node config = new VBox(8, simulationConfig, derivativeConfig);

        Node graph = graphBox();

        SplitPane sp = new SplitPane(config, graph);

        sp.getDividers().getFirst().positionProperty()
                .addListener((_, _, _) -> {
                    double val = 225 / sp.getWidth();
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