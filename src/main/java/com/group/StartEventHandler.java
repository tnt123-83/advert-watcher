package com.group;

import com.group.dto.AdvertDto;
import com.group.service.AdvertService;
import com.group.service.GenerateDataService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class StartEventHandler implements EventHandler<ActionEvent> {
    private Button button;
    private ImageView progressGif;
    private final TabPane tabPane;
    private TextArea output;
    private GenerateDataService service;
    private AdvertService advertService;
    private PartialResultsTask workingTask;
    private final Map<String, ObservableList<AdvertDto>> mapTabNameToItems = new HashMap<>();

    StartEventHandler(Button button, ImageView progressGif, TabPane tabs, TextArea output, AdvertService advertService,
                      GenerateDataService service) {
        this.button = button;
        this.progressGif = progressGif;
        this.tabPane = tabs;
        this.output = output;
        this.advertService = advertService;
        this.service = service;
    }

    public boolean isTaskFinished() {
        return workingTask == null || !workingTask.isRunning();
    }

    @Override
    public void handle(ActionEvent event) {
        if (workingTask != null && !workingTask.isCancelled() && !workingTask.isDone()) {
            service.setWorkingTaskCancelled(true);
            workingTask.cancel();
            button.setText("Start");
            progressGif.setVisible(false);
        } else {
            tabPane.getTabs().removeAll(tabPane.getTabs());
            mapTabNameToItems.clear();
            service.getResultTablesNames().forEach(name -> createTabWithNameAndAddToMap(name, mapTabNameToItems, tabPane));
            button.setText("Stop");
            progressGif.setVisible(true);
            service.setWorkingTaskCancelled(false);
            workingTask = new PartialResultsTask(service);
            configureTask(workingTask);
            new Thread(workingTask).start();
        }
    }

    private void configureTask(PartialResultsTask workingTask) {
        workingTask.setOnSucceeded(e -> {
            button.setText("Start");
            progressGif.setVisible(false);
        });
        workingTask.getPartialResults().addListener((ListChangeListener<AdvertDto>) c -> {
            while (c.next()) {
                for (AdvertDto additem : c.getAddedSubList()) {
                    distributeAdvertsByTabs(additem, tabPane, mapTabNameToItems, advertService);
                }
            }
        });
    }

    public static void distributeAdvertsByTabs(AdvertDto additem, TabPane tabPane, Map<String,
            ObservableList<AdvertDto>> mapTabNameToItems, AdvertService advertService) {
        additem.getSave().addListener((observable, oldValue, newValue) -> {
            additem.getSave().setValue(newValue.booleanValue());
            advertService.update(additem, newValue.booleanValue());
        });
        additem.getViewed().addListener((observable, oldValue, newValue) -> {
            additem.getViewed().setValue(newValue.booleanValue());
        });
        additem.getNew_().addListener((observable, oldValue, newValue) -> {
            additem.getNew_().setValue(newValue.booleanValue());
        });

        if (mapTabNameToItems.containsKey(additem.getFilter().getGroupName())) {
            if (additem.getViewed().getValue()) {
                mapTabNameToItems.get(additem.getFilter().getGroupName()).add(additem);
            } else {
                mapTabNameToItems.get(additem.getFilter().getGroupName()).add(0, additem);
            }
        } else {
            createTabWithNameAndAddToMap(additem.getFilter().getGroupName(), mapTabNameToItems, tabPane);
            mapTabNameToItems.get(additem.getFilter().getGroupName()).add(additem);
        }
    }

    private static void createTabWithNameAndAddToMap(String name, Map<String, ObservableList<AdvertDto>> mapTabNameToItems, TabPane tabPane) {
        if (mapTabNameToItems.containsKey(name)) return;
        mapTabNameToItems.put(name, FXCollections.observableArrayList(new ArrayList()));
        Tab tab = new Tab(name);
        TableView t = new TableView();
        SpringStageLoader.configureTable(t);
        t.setItems(mapTabNameToItems.get(name));
        tab.setContent(t);
        tabPane.getTabs().add(tab);
    }
}
