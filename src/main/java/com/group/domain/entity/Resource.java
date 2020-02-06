package com.group.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Resource {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("parentId")
    private Long parentId;
    @JsonProperty("url")
    private String url;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("locators")
    private Map<String, String> locators;
    private List<Filter> filters;

    public Resource() {
        this.id = 0L;
        this.parentId = 0L;
        this.url = "";
        this.prefix = "";
        this.locators = new HashMap<>();
        this.filters = new ArrayList<>();
    }
}
