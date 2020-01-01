package com.group;

import com.group.dto.AdvertDto;
import com.group.service.GenerateDataService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;

import java.util.ArrayList;

public class PartialResultsTask extends Task<ObservableList<AdvertDto>> {
    private final GenerateDataService service;

    private ReadOnlyObjectWrapper<ObservableList<AdvertDto>> partialResults
            = new ReadOnlyObjectWrapper<>(this, "partialResults",
            FXCollections.observableArrayList(new ArrayList()));

    public PartialResultsTask(GenerateDataService service) {
        this.service = service;
    }

    public final ObservableList getPartialResults() {
        return partialResults.get();
    }

    @Override
    protected ObservableList call() throws Exception {
        service.runGenerator();
        while (true) {
            if (service.isDataTransmited()) {
                break;
            } else {
                Platform.runLater(() -> getPartialResults().addAll(service.generateData()));
                Thread.sleep(1000);
            }
        }
        return partialResults.get();
    }

}
