package com.group.service;

import com.group.dto.AdvertDto;

import java.util.ArrayDeque;
import java.util.List;

public interface GenerateDataService {
    void runGenerator();
    ArrayDeque<AdvertDto> generateData();
    Boolean isDataTransmited();
    void setWorkingTaskCancelled(Boolean status);
    List<String> getResultTablesNames();
}
