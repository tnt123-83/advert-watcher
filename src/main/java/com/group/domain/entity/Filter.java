package com.group.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    @JsonProperty("resourceIds")
    private List<Long> resourceIds;
    @JsonProperty("contains")
    private Map<String, String> contains;
    @JsonProperty("color")
    private String color;
    @JsonProperty("groupName")
    private String groupName;
}
