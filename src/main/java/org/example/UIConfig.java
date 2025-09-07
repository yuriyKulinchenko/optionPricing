package org.example;

import javafx.application.Application;
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
        public SimpleStringProperty simulationType;

        public SimulationConfigState() {
            timeStepCount = new SimpleStringProperty("100");
            simulationCount = new SimpleStringProperty("10000");
            simulationType = new SimpleStringProperty("Geometric Brownian motion");
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
    }

    abstract public static class DerivativeState {

        public static class European extends DerivativeState {
            public SimpleStringProperty isCall;
            public SimpleStringProperty strike;
            public SimpleStringProperty maturity;

            public European() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("120");
                this.maturity = new SimpleStringProperty("1");
            }
        }

        public static class Asian extends DerivativeState {
            public SimpleStringProperty isCall;
            public SimpleStringProperty strike;
            public SimpleStringProperty maturity;
            public SimpleStringProperty averagingType;
            public SimpleStringProperty averagingFrequency;

            public Asian() {
                this.isCall = new SimpleStringProperty("true");
                this.strike = new SimpleStringProperty("120");
                this.maturity = new SimpleStringProperty("1");
                this.averagingType = new SimpleStringProperty("Arithmetic");
                this.averagingFrequency = new SimpleStringProperty("Continuous");
            }
        }

        public static class Barrier extends DerivativeState {
            public SimpleStringProperty barrier;
            public SimpleStringProperty barrierType;
            public SimpleObjectProperty<DerivativeState> underlying;

            public Barrier() {
                this.barrier = new SimpleStringProperty("120");
                this.barrierType = new SimpleStringProperty("Up-and-In");
                this.underlying = new SimpleObjectProperty<>(new European());
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

    private ComboBox<String> createComboBox(String... strings) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(strings);
        box.setValue(strings[0]);
        box.setMaxWidth(200);
        return box;
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

        ComboBox<String> optionSelector = createComboBox("European", "Asian", "Barrier");

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
            setPricerResult();
        });

        btn.setMaxWidth(200);

        VBox config = new VBox(8.,
                title,
                form,
                new Label("Derivative type:"),
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

    private void setPricerResult() {
        // Parse config state:

        double spot = Double.parseDouble(configState.spot.get());
        double interest = Double.parseDouble(configState.interest.get());
        double vol = Double.parseDouble(configState.vol.get());

        // Parse simulation state:

        int simulationCount = Integer.parseInt(simulationState.simulationCount.get());
        int stepCount = Integer.parseInt(simulationState.timeStepCount.get());
        String simulationType = simulationState.simulationType.get();

        DerivativePricer pricer = new MonteCarloPricer.Builder()
                .setIterationCount(simulationCount)
                .setSteps(stepCount)
                .setRate(interest)
                .build();

        // Parse derivative:

        Supplier<StochasticProcess> supplier = switch (simulationType) {
            case "Geometric Brownian motion" -> () -> new GeometricBrownianMotion(spot, interest, vol);
            case "Jump diffusion" -> () -> new JumpDiffusion(spot, interest, vol);
            default -> null;
        };

        Derivative derivative = getDerivative(derivativeState.get());
        pricerResult.set(pricer.getPrice(derivative, supplier));
    }

    private Derivative getDerivative(DerivativeState state) {

        Derivative derivative = null;

        if(state instanceof DerivativeState.European european) {

            double strike = Double.parseDouble(european.strike.get());
            double maturity = Double.parseDouble(european.maturity.get());
            derivative = new EuropeanOption(Option.Type.CALL, strike, maturity);

        } else if(state instanceof DerivativeState.Asian asian) {

            double strike = Double.parseDouble(asian.strike.get());
            double maturity = Double.parseDouble(asian.maturity.get());
            AsianOption.AveragingType averagingType = switch (asian.averagingType.get()) {
                case "Arithmetic" -> AsianOption.AveragingType.ARITHMETIC;
                case "Geometric" -> AsianOption.AveragingType.GEOMETRIC;
                default -> null;
            };
            derivative = new AsianOption(Option.Type.CALL, strike, maturity, averagingType);

        } else if(state instanceof DerivativeState.Barrier barrier) {

            double barrierPrice = Double.parseDouble(barrier.barrier.get());
            Barrier.Type barrierType = switch (barrier.barrierType.get()) {
                case "Up-and-In" -> Barrier.Type.UP_IN;
                case "Up-and-Out" -> Barrier.Type.UP_OUT;
                case "Down-and-In" -> Barrier.Type.DOWN_IN;
                case "Down-and-Out" -> Barrier.Type.DOWN_OUT;
                default -> null;
            };

            Derivative underlying = getDerivative(barrier.underlying.get());
            derivative = new Barrier(underlying, barrierPrice, barrierType); // Tweak configuration settings

        }

        return derivative;
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

        return new VBox(8, form);
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

        ComboBox<String> averagingTypeSelector = createComboBox("Arithmetic", "Geometric");

        averagingTypeSelector.setOnAction(e -> {
            String selected = averagingTypeSelector.getValue();
            ((DerivativeState.Asian) derivativeState.get()).averagingType.set(selected);
        });

        ComboBox<String> averagingFrequencySelector = createComboBox("Continuous", "Weekly", "Monthly");

        averagingTypeSelector.setOnAction(e -> {
            String selected = averagingFrequencySelector.getValue();
            ((DerivativeState.Asian) derivativeState.get()).averagingFrequency.set(selected);
        });



        return new VBox(8,
                form,
                new Label("Averaging type:"),
                averagingTypeSelector,
                new Label("Averaging frequency:"),
                averagingFrequencySelector
        );
    }

    private Node barrierConfigBox() {
        DerivativeState.Barrier barrierState = (DerivativeState.Barrier) derivativeState.get();
        GridPane form = createConfigForm();
        form.addRow(0, new Label("Barrier price"), createConfigField(barrierState.barrier));

        ComboBox<String> typeSelector = createComboBox(
                "Up-and-In", "Up-and-Out",
                "Down-and-In", "Down-and-Out"
        );

        typeSelector.setOnAction(e -> {
            String selected = typeSelector.getValue();
            ((DerivativeState.Barrier)derivativeState.get()).barrierType.set(selected);
        });

        StackPane derivativeConfig = new StackPane();
        derivativeConfig.getChildren().add(europeanConfigBox(true));

        ComboBox<String> optionSelector = createComboBox("European", "Asian");

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
                form,
                new Label("Barrier type:"),
                typeSelector,
                new Label("Underlying option:"),
                optionSelector,
                separator,
                derivativeConfig
                );
    }

    private Node simulationConfigBox() {
        Label title = new Label("Simulation configuration");
        title.setStyle("-fx-font-size: 16px;");

        GridPane form = createConfigForm();

        ComboBox<String> optionSelector = createComboBox("Geometric Brownian motion", "Jump diffusion");

        optionSelector.setOnAction(e -> {
            String selected = optionSelector.getValue();
            simulationState.simulationType.set(selected);
        });


        form.addRow(0, new Label("Path count"), createConfigField(simulationState.simulationCount));
        form.addRow(1, new Label("Time steps"), createConfigField(simulationState.timeStepCount));


        VBox config = new VBox(8, title, optionSelector, form);
        config.setPadding(new Insets(10));
        config.setAlignment(Pos.TOP_LEFT);
        return config;
    }

    private Node outputBox() {

        Label title = new Label("Graphs");
        title.setStyle("-fx-font-size: 16px;");

        HBox graphs = new HBox(8,
                UIGraph.simulationChart,
                UIGraph.varianceChart
        );

        Label estimatedPriceLabel = new Label("Estimated price: 0.00");
        VBox results = new VBox(8, estimatedPriceLabel);

        pricerResult.addListener((_, _, val) -> {
            UIGraph.populateSimulationChart(val.paths);
            UIGraph.populateVarianceChart(val.sums, val.squares, val.chunkSize);
            estimatedPriceLabel.textProperty().set("Estimated price: " + val.derivativePrice);
        });


        graphs.setPadding(new Insets(10));
        graphs.setAlignment(Pos.TOP_LEFT);

        VBox output = new VBox(8.,
                graphs,
                results
        );

        output.setPadding(new Insets(10));
        output.setAlignment(Pos.TOP_LEFT);

        return output;
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

        Node config = new VBox(8,
                simulationConfig,
                getSeparator(Orientation.HORIZONTAL),
                derivativeConfig
        );

        Node graph = outputBox();

        SplitPane sp = new SplitPane(config, graph);

        sp.getDividers().getFirst().positionProperty()
                .addListener((_, _, _) -> {
                    double val = 225 / sp.getWidth();
                    sp.setDividerPositions(val);
                });

        Scene scene = new Scene(sp, 1200, 800);


        stage.setTitle("Option pricer");
        stage.setScene(scene);
        stage.show();
        config.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}