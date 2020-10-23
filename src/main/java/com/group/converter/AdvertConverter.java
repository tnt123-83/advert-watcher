package com.group.converter;

import com.group.domain.entity.Advert;
import com.group.domain.entity.Filter;
import com.group.dto.AdvertDto;
import javafx.beans.property.SimpleBooleanProperty;
import org.springframework.stereotype.Component;

@Component
public class AdvertConverter extends CustomConverter<Advert, AdvertDto> {
    @Override
    public Advert toEntity(AdvertDto dto) {
        Advert advert = super.toEntity(dto);
        advert.setGroupName(dto.getFilter().getGroupName());
        advert.setViewed(dto.getViewed().getValue());
        advert.setSave(dto.getSave().getValue());
        return advert;
    }

    @Override
    public AdvertDto toDto(Advert entity) {
        AdvertDto advertDto = super.toDto(entity);
        advertDto.setViewed(new SimpleBooleanProperty(entity.isViewed()));
        advertDto.setSave(new SimpleBooleanProperty(entity.isSave()));
        advertDto.setNew_(new SimpleBooleanProperty(true));
        Filter f = new Filter();
        f.setGroupName(entity.getGroupName());
        advertDto.setFilter(f);
        return advertDto;
    }
}
