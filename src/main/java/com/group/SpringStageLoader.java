package com.group;

import com.group.dto.AdvertDto;
import com.group.service.AdvertService;
import com.group.service.GenerateDataService;
import com.group.util.HyperlinkCell;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class SpringStageLoader implements ApplicationContextAware {
    private static Group root = new Group();

    public static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final double OPACITY = 0.3;

    private static final String ADVERTS = "Adverts";

    private static HostServices hostServices;

    private static HBox columnsBox = new HBox();
    private static HBox bottomBox = new HBox();
    private static VBox columnA = new VBox();
    private static VBox columnB = new VBox();

    private static TabPane tabPane = new TabPane();
    private static TextArea output = new TextArea();
    private static Button startButton = new Button();
    private static TextField startEveryTF = new TextField();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private StartEventHandler startEventHandler;

    @Autowired
    private GenerateDataService generateDataService;

    @Autowired
    private AdvertService advertService;

    @Resource(name = "params")
    private Map<String, String> params;

    private static ApplicationContext staticContext;

    public Stage loadMain(Stage stage) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add("application.css");
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
        startEventHandler = new StartEventHandler(startButton, progressGif, tabPane, output, advertService, generateDataService);
        startButton.setOnAction(startEventHandler);
        Button loadFromDB = new Button("Load From DB");
        loadFromDB.setOnAction(action -> restoreAdvertsFromDB(tabPane));
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

        columnA.getChildren().addAll(tabPane, output);
    }

    private void configureTabs() {
        TableView table = new TableView();
        configureTable(table);
        Tab tab = new Tab();
        tab.setContent(table);
        tabPane.getTabs().add(tab);
        if ("true".equals(params.get("isRestoreOnStartEnabled"))) {
            restoreAdvertsFromDB(tabPane);
        }
    }

    private void restoreAdvertsFromDB(TabPane tabPane) {
        Map<String, ObservableList<AdvertDto>> mapTabNameToItems = new HashMap<>();
        tabPane.getTabs().removeAll(tabPane.getTabs());
        List<AdvertDto> all = advertService.findAll();
        all.forEach(a -> StartEventHandler.distributeAdvertsByTabs(a, tabPane, mapTabNameToItems, advertService));
        generateDataService.setFoundAdverts(new HashSet<>(all));
    }

    public static void configureTable(TableView t) {
        //table config
        t.setEditable(true);
        t.setMaxSize(WIDTH * 3 / 4, HEIGHT * 2 / 3);
        t.setRowFactory((Callback) rf -> {
            TableRow<AdvertDto> row = new TableRow<>();
            System.out.println("cdddd");
            row.setStyle("-fx-text-background-color: blue;");
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    AdvertDetailsModalStage stage = new AdvertDetailsModalStage();
                    stage.showDetails(row.getItem());
                }
            });

            return row;
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
                return new CheckBoxTableCell<AdvertDto, Boolean>() {
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
            }
        });

        isViewedColumn.setCellValueFactory(cellData -> cellData.getValue().getViewed());
        isViewedColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Hyperlink> linkColumn = new TableColumn<>("Link");
        linkColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(new Hyperlink(v.getValue().getUrl())));
        HyperlinkCell.setHostServices(hostServices);
        //linkColumn.setCellFactory(new HyperlinkCell());
        linkColumn.setCellFactory(new Callback<TableColumn<AdvertDto, Hyperlink>, TableCell<AdvertDto, Hyperlink>>() {
            @Override
            public TableCell<AdvertDto, Hyperlink> call(TableColumn<AdvertDto, Hyperlink> arg) {

                return new TableCell<AdvertDto, Hyperlink>() {
                    private final Hyperlink hyperlink = new Hyperlink();
                    {
                        hyperlink.setOnMouseClicked(event -> {
                            Hyperlink url = getItem();
                            AdvertDto advert = getTableRow().getItem();
                            advert.getNew_().setValue(false);
                            if (getTableRow() != null) {
                                getTableRow().setStyle("");
                            }
                            url.setVisited(true);

                            //hostServices.showDocument(url.getText());
                        });
                    }

                    @Override
                    protected void updateItem(Hyperlink url, boolean empty) {
                        //System.out.println(getTableRow().getItem());
                        super.updateItem(url, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            //System.out.println(url.isVisited());
                            if (!url.isVisited()) {
                                hyperlink.setText(url.getText());
                                setGraphic(hyperlink);
                                if (getTableRow().getItem() != null) {
                                    System.out.println(getTableRow().getItem().getPrice()+"  "+getTableRow().getItem().getNew_());
                                    if (getTableRow().getItem().getNew_().get()) {
                                        getTableRow().setStyle("-fx-text-background-color: blue;");
                                    } else {
                                        url.setVisited(true);
                                        getTableRow().setStyle("");
                                    }
                                }
                            }
                        }
                    }
                };
            }
        });
        linkColumn.prefWidthProperty().set(120);

        // столбец
        TableColumn<AdvertDto, Integer> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getPrice()));
        priceColumn.prefWidthProperty().set(70);

        // столбец
        TableColumn<AdvertDto, Integer> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getLocation()));
        locationColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Integer> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getDescription()));
        descriptionColumn.prefWidthProperty().set(50);

        // столбец
        TableColumn<AdvertDto, Integer> textColumn = new TableColumn<>("Text");
        textColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper(v.getValue().getText()));
        textColumn.prefWidthProperty().set(300);

        t.getColumns().addAll(numberCol, saveColumn, isViewedColumn, linkColumn, priceColumn, locationColumn, descriptionColumn, textColumn);
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
        prefiltration.setSelected("true".equals(params.get("isPrefiltrationEnabled")));
        prefiltration.setOnAction(actionEvent ->
                params.put("isPrefiltrationEnabled", Boolean.toString(prefiltration.isSelected())));
        Label prefiltrationL = new Label("Prefiltration");
        HBox prefiltrationB = new HBox();
        prefiltrationB.setSpacing(10);
        prefiltrationB.getChildren().addAll(prefiltration, prefiltrationL);

        CheckBox restoreOnStart = new CheckBox();
        restoreOnStart.setSelected("true".equals(params.get("isRestoreOnStartEnabled")));
        restoreOnStart.setOnAction(actionEvent ->
                params.put("isRestoreOnStartEnabled", Boolean.toString(restoreOnStart.isSelected())));
        Label restoreOnStartL = new Label("Restore on Start");
        HBox restoreOnStartB = new HBox();
        restoreOnStartB.setSpacing(10);
        restoreOnStartB.getChildren().addAll(restoreOnStart, restoreOnStartL);

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
        clearDB.setOnAction(action -> advertService.deleteAll());
        TextField clearDBTF = new TextField();
        clearDBTF.setPrefColumnCount(3);
        Label clearDBTFL = new Label("strings");
        HBox clearDBB = new HBox();
        clearDBB.setSpacing(10);
        clearDBB.getChildren().addAll(clearDB, clearDBL, clearDBTF, clearDBTFL);
        clearDBB.setDisable(false);

        columnB.getChildren().addAll(priceB, saveToBDB, startEveryB, prefiltrationB, restoreOnStartB, showXStringsB, clearDBB);
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