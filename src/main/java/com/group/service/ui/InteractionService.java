package com.group.service.ui;

import com.group.dto.AdvertDto;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

import java.util.List;
import java.util.Map;

public interface InteractionService {
    void createTabsWithNames(List<String> resultTablesNames, Map<String, ObservableList<AdvertDto>> mapTabNameToItems, TabPane tabs);
}
