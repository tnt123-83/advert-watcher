package com.group.service.ui.impl;

import com.group.dto.AdvertDto;
import com.group.service.ui.InteractionService;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InteractionServiceImpl implements InteractionService {
    @Override
    public void createTabsWithNames(List<String> resultTablesNames, Map<String, ObservableList<AdvertDto>> mapTabNameToItems, TabPane tabs) {

    }
}
