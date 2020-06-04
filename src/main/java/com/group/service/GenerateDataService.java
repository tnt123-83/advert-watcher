package com.group.service;

import com.group.dto.AdvertDto;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;

public interface GenerateDataService {
    void runGenerator();
    ArrayDeque<AdvertDto> generateData();
    Boolean isDataTransmited();
    void setWorkingTaskCancelled(Boolean status);
    List<String> getResultTablesNames();
    void setFoundAdverts(Set<AdvertDto> foundAdverts);
}
