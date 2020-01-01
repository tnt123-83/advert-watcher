package com.group;

import com.group.dto.AdvertDto;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdvertDetailsModalStage extends Stage {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String ADVERTS = "Adverts";

    private Group root = new Group();
    private HBox columnsBox = new HBox();
    private HBox bottomBox = new HBox();
    private VBox columnA = new VBox();
    private VBox columnB = new VBox();

    private TextField id = new TextField();
    private TextField siteId = new TextField();
    private TextField url = new TextField();
    private TextField title = new TextField();
    private TextField description = new TextField();
    private TextArea text = new TextArea();
    private TextField price = new TextField();
    private TextField date = new TextField();
    private TextField location = new TextField();
    private TextField fromAgent = new TextField();

    public AdvertDetailsModalStage() {
        this.initModality(Modality.WINDOW_MODAL);
        this.centerOnScreen();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        this.setScene(scene);
        configureScene(scene);
    }

    private void configureScene(Scene scene) {
        configureColumnA();
        configureColumnB();
        configureBottomB();

        columnsBox.getChildren().add(columnA);
        columnsBox.getChildren().add(columnB);
        root.getChildren().addAll(columnsBox, bottomBox);
    }

    private void configureColumnA() {
        columnA.setPadding(new Insets(10, 30, 10, 30));
        columnA.setSpacing(20);

        Label titleL = new Label("Title:");
        title.setPrefColumnCount(20);
        HBox titleB = new HBox();
        titleB.getChildren().addAll(titleL, title);
        titleB.setSpacing(10);

        Label urlL = new Label("Url:");
        url.setPrefColumnCount(20);
        HBox urlB = new HBox();
        urlB.getChildren().addAll(urlL, url);
        urlB.setSpacing(10);

        Label descriptionL = new Label("Description:");
        description.setPrefColumnCount(20);
        HBox descriptionB = new HBox();
        descriptionB.getChildren().addAll(descriptionL, description);
        descriptionB.setSpacing(10);

        text.setText("Text");
        text.setWrapText(true);
        text.setMaxSize(WIDTH * 2 / 3, HEIGHT * 3 / 5);

        columnA.getChildren().addAll(titleB, urlB, descriptionB, text);
    }

    private void configureColumnB() {
        columnB.setPadding(new Insets(10, 30, 10, 30));
        columnB.setSpacing(20);

        Label priceL = new Label("Price:");
        price.setPrefColumnCount(7);
        HBox priceB = new HBox();
        priceB.getChildren().addAll(priceL, price);
        priceB.setSpacing(10);

        Label dateL = new Label("Date:");
        date.setPrefColumnCount(20);
        HBox dateB = new HBox();
        dateB.getChildren().addAll(dateL, date);
        dateB.setSpacing(10);

        Label locationL = new Label("Location:");
        location.setPrefColumnCount(20);
        HBox locationB = new HBox();
        locationB.getChildren().addAll(locationL, location);
        locationB.setSpacing(10);

        Label fromAgentL = new Label("Owner:");
        fromAgent.setPrefColumnCount(20);
        HBox fromAgentB = new HBox();
        fromAgentB.getChildren().addAll(fromAgentL, fromAgent);
        fromAgentB.setSpacing(10);

        columnB.getChildren().addAll(priceB, dateB, locationB, fromAgentB);
    }

    private void configureBottomB() {
        Button close = new Button("Close");
        close.setOnAction(actionEvent -> {
            Stage stage = (Stage) close.getScene().getWindow();
            stage.close();
        });
        bottomBox.setLayoutX(WIDTH - 100);
        bottomBox.setLayoutY(HEIGHT - 50);
        bottomBox.setSpacing(10);
        bottomBox.getChildren().addAll(close);
    }

    public void showDetails(AdvertDto advert) {
        title.setText(advert.getTitle());
        url.setText(advert.getUrl());
        description.setText(advert.getDescription());
        text.setText(advert.getText());
        price.setText(advert.getPrice());
        date.setText(advert.getDate().toString());
        location.setText(advert.getLocation());
        setTitle("Details: " + advert.getSiteId());
        show();
    }
}