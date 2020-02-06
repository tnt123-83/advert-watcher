package com.group.dto;

import com.group.domain.entity.Filter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvertDto {
    private Long id;
    private String siteId;
    private String url;
    private String title;
    private String description;
    private String text;
    private String price;
    private Date date;
    private String location;
    private String fromAgent;
    private Filter filter;
    private BooleanProperty viewed = new SimpleBooleanProperty();
    private BooleanProperty save = new SimpleBooleanProperty();

    public boolean equals(AdvertDto dto) {
        if (this.filter != null && this.filter.equals(dto.getFilter())
                && this.getPrice() != null && this.price.equals(dto.getPrice())) {
            if (this.getUrl() != null && this.url.equals(dto.getUrl())) return true;
            if (this.getText() != null && this.text.equals(dto.getText())) return true;
        }
        return false;
    }
}
