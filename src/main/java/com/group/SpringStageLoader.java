package com.group;

import com.group.dto.AdvertDto;
import com.group.service.AdvertService;
import com.group.service.GenerateDataService;
import com.group.service.ui.InteractionService;
import com.group.util.HyperlinkCell;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Component
public class SpringStageLoader implements ApplicationContextAware {
    private static Group root = new Group();

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final double OPACITY = 0.3;

    private static final String ADVERTS = "Adverts";

    private static HostServices hostServices;

    private static HBox columnsBox = new HBox();
    private static HBox bottomBox = new HBox();
    private static VBox columnA = new VBox();
    private static VBox columnB = new VBox();

    private static TabPane tabs = new TabPane();
    private static TextArea output = new TextArea();
    private static Button startButton = new Button();
    private static TextField startEveryTF = new TextField();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> clickStartButtonHandleTask;
    private StartEventHandler startEventHandler;

    @Autowired
    private GenerateDataService generateDataService;

    @Autowired
    private AdvertService advertService;

    @Resource(name = "params")
    private Map<String, String> params;

    private static ApplicationContext staticContext;
    private boolean isPrefiltrationEnabled;

    public Stage loadMain(Stage stage) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle(ADVERTS);
        stage.setScene(scene);
        stage.show();
        stage.setScene(scene);
        hostServices = (HostServices)stage.getProperties().get("hostServices");
        configureColumnA();
        configureColumnB();
        configureBottomB();

        columnsBox.getChildren().add(columnA);
        columnsBox.getChildren().add(columnB);

        root.getChildren().addAll(columnsBox, bottomBox);
        return stage;
    }

    private void configureBottomB() {
        ImageView progressGif = new ImageView();
        Image i = new Image(new File("src/main/resources/in_progress.gif").toURI().toString());
        progressGif.setImage(i);
        progressGif.setVisible(false);
        startButton.setText("Start");
        startEventHandler = new StartEventHandler(startButton, progressGif, tabs, output, advertService, generateDataService);
        startButton.setOnAction(startEventHandler);
        Button loadFromDB = new Button("Load From DB");
        loadFromDB.setOnAction(actionEvent -> Platform.exit());
        Button exit = new Button("Exit");
        exit.setOnAction(actionEvent -> Platform.exit());
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setSpacing(10);
        bottomBox.getChildren().addAll(progressGif, startButton, loadFromDB, exit);
        bottomBox.setLayoutX(WIDTH - 300);
        bottomBox.setLayoutY(HEIGHT - 50);
    }

    private void configureColumnA() {
        columnA.setPadding(new Insets(10, 30, 10, 30));
        columnA.setSpacing(20);
        columnA.setPrefWidth(WIDTH * 3 / 4);
        configureTabs();

        configureOutputTextArea();

        columnA.getChildren().addAll(tabs, output);
    }

    private void configureTabs() {
        TableView table = new TableView();
        configureTable(table);
        Tab tab = new Tab();
        tab.setContent(table);
        tabs.getTabs().add(tab);
    }

    public static void configureTable(TableView t) {
        //table config
        t.setEditable(true);
        t.setMaxSize(WIDTH * 3 / 4, HEIGHT * 2 / 3);
        t.setRowFactory(new Callback() {
            @Override
            public Object call(Object rf) {
                TableRow<AdvertDto> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        AdvertDetailsModalStage stage = new AdvertDetailsModalStage();
                        stage.showDetails(row.getItem());
                    }
                });
                return row;
            }
        });

        // столбец
        TableColumn numberCol = new TableColumn("#");
        numberCol.setCellValueFactory((Callback<TableColumn.CellDataFeatures<AdvertDto, String>, ObservableValue<String>>)
                p -> new ReadOnlyObjectWrapper((t.getItems().indexOf(p.getValue()) + 1) + ""));
        //numberCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        numberCol.prefWidthProperty().set(30);

        numberCol.setSortable(false);

        // столбец
//        TableColumn<AdvertDto, String> idColumn = new TableColumn<>("Id");
//        idColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getId()));
//        idColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Boolean> saveColumn = new TableColumn<>("Save");
        saveColumn.setCellFactory(tc -> new CheckBoxTableCell<AdvertDto, Boolean>());

        saveColumn.setCellValueFactory(cellData -> cellData.getValue().getSave());
        saveColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Boolean> isViewedColumn = new TableColumn<>("Viewed");
        isViewedColumn.setCellFactory(new Callback<TableColumn<AdvertDto, Boolean>, TableCell<AdvertDto, Boolean>>() {
            @Override
            public TableCell<AdvertDto, Boolean> call(TableColumn<AdvertDto, Boolean> tc) {
                CheckBoxTableCell<AdvertDto, Boolean> a = new CheckBoxTableCell<AdvertDto, Boolean>() {
                    @Override
                    public void updateItem(Boolean item, boolean empty) {
                        boolean a = item == null ? false : item;
                        Double opacity = a ? OPACITY : 1.0;
                        super.updateItem(item, empty);
                        if (getTableRow() != null) {
                            //getTableRow().setOpacity(opacity);
                            for (int i = 0; i < getTableRow().getChildrenUnmodifiable().size(); i++) {
                                getTableRow().getChildrenUnmodifiable().get(i).setOpacity(opacity);
                            }
                        }
                    }
                };
                return a;
            }
        });

        isViewedColumn.setCellValueFactory(cellData -> cellData.getValue().getViewed());
        isViewedColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Hyperlink> linkColumn = new TableColumn<>("Link");
        linkColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(new Hyperlink(v.getValue().getUrl())));
        HyperlinkCell.setHostServices(hostServices);
        linkColumn.setCellFactory(new HyperlinkCell());
        linkColumn.prefWidthProperty().set(120);

        // столбец
        TableColumn<AdvertDto, Integer> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getPrice()));
        priceColumn.prefWidthProperty().set(70);

        // столбец
        TableColumn<AdvertDto, Integer> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getDescription()));
        descriptionColumn.prefWidthProperty().set(80);

        // столбец
        TableColumn<AdvertDto, Integer> textColumn = new TableColumn<>("Text");
        textColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getText()));
        textColumn.prefWidthProperty().set(300);

        t.getColumns().addAll(numberCol, saveColumn, isViewedColumn, linkColumn, priceColumn, descriptionColumn, textColumn);
    }

    public void configureOutputTextArea() {
        output.setText("Output");
        output.setMaxSize(WIDTH * 2 / 3, HEIGHT / 6);
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendText(String.valueOf((char) b));
            }
        };
        //System.setOut(new PrintStream(out, true));
    }

    private void configureColumnB() {
        columnB.setPadding(new Insets(10, 30, 10, 30));
        columnB.setSpacing(20);

        Label $priceL = new Label("$ price:");
        TextField $price = new TextField(params.get("exchange.usd"));
        configureTextField("exchange.usd", $price);

        HBox priceB = new HBox();
        priceB.getChildren().addAll($priceL, $price);
        priceB.setSpacing(10);

        CheckBox saveToDB = new CheckBox();
        Label saveToDBL = new Label("Save to DB");
        HBox saveToBDB = new HBox();
        saveToBDB.getChildren().addAll(saveToDB, saveToDBL);
        saveToBDB.setSpacing(10);
        saveToBDB.setDisable(true);

        CheckBox startEvery = new CheckBox();
        ScheduledService<Void> scheduledService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                Task<Void> a = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        return null;
                    }
                };
                return a;
            }
        };
        startEvery.setOnAction(actionEvent -> {
            if (startEvery.isSelected()) {
                scheduledService.setOnSucceeded(e -> {
                    System.out.println(startEventHandler.isTaskFinished());
                    if (startEventHandler.isTaskFinished()) startEventHandler.handle(null);
                });
                scheduledService.setPeriod(Duration.minutes(Integer.parseInt(startEveryTF.getText())));
                scheduledService.reset();
                scheduledService.start();
            } else {
                if (scheduledService != null) scheduledService.cancel();
            }
        });
        Label startEveryL = new Label("Start every");
        startEveryTF.setText(params.get("start.every"));
        startEveryTF.setPrefColumnCount(3);
        configureTextField("start.every", startEveryTF);
        Label startEveryTFL = new Label("mins");
        HBox startEveryB = new HBox();
        startEveryB.setSpacing(10);
        startEveryB.getChildren().addAll(startEvery, startEveryL, startEveryTF, startEveryTFL);

        CheckBox prefiltration = new CheckBox();
        prefiltration.setOnAction(actionEvent -> {
            if (prefiltration.isSelected()) {
                isPrefiltrationEnabled = true;
            } else {
                isPrefiltrationEnabled = false;
            }
        });
        Label prefiltrationL = new Label("Prefiltration");
        HBox prefiltrationB = new HBox();
        prefiltrationB.setSpacing(10);
        prefiltrationB.getChildren().addAll(prefiltration, prefiltrationL);

        CheckBox observation = new CheckBox();
        observation.setOnAction(actionEvent -> {
            if (observation.isSelected()) {

            } else {

            }
        });
        Label observationL = new Label("Observation");
        HBox observationB = new HBox();
        observationB.setSpacing(10);
        observationB.getChildren().addAll(observation, observationL);

        CheckBox showXStrings = new CheckBox();
        Label showXStringsL = new Label("Show");
        TextField showXStringsTF = new TextField();
        showXStringsTF.setPrefColumnCount(3);
        Label showXStringsTFL = new Label("strings");
        HBox showXStringsB = new HBox();
        showXStringsB.setSpacing(10);
        showXStringsB.getChildren().addAll(showXStrings, showXStringsL, showXStringsTF, showXStringsTFL);
        showXStringsB.setDisable(true);

        CheckBox clearDB = new CheckBox();
        Label clearDBL = new Label("Clear DB to");
        TextField clearDBTF = new TextField();
        clearDBTF.setPrefColumnCount(3);
        Label clearDBTFL = new Label("strings");
        HBox clearDBB = new HBox();
        clearDBB.setSpacing(10);
        clearDBB.getChildren().addAll(clearDB, clearDBL, clearDBTF, clearDBTFL);
        clearDBB.setDisable(true);

        columnB.getChildren().addAll(priceB, saveToBDB, startEveryB, prefiltrationB, observationB, showXStringsB, clearDBB);
    }

    private void configureTextField(String property, TextField textField) {
        textField.setPrefColumnCount(3);
        textField.setOnKeyPressed(keyEvent -> {
            String temp = "";
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (!(temp.equals(textField.getText()))) {
                    temp = textField.getText();
                    params.put(property, temp);
                }
            }
        });
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            String temp = "";
            //focus in
            if (newValue) {
                temp = textField.getText();
            }
            //focus out
            if (oldValue) {
                if (!(temp.equals(textField.getText()))) {
                    params.put(property, textField.getText());
                }
            }
        });
    }

    public void saveParamChanges() {
        try {
            // create and set properties into properties object
            Properties props = new Properties();
            props.putAll(params);
            // get or create the file
            File f = new File("src/main/resources/run.properties");
            OutputStream out = new FileOutputStream(f);
            // write into it
            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
            p.store(props, out, null);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Передаем данные в статические поля в реализации метода интерфейса ApplicationContextAware,
     * т.к. методы их использующие тоже статические
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringStageLoader.staticContext = context;
    }

    public void appendText(String str) {
        Platform.runLater(() -> output.appendText(str));
    }
}