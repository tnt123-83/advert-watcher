package com.group.service;

import com.group.domain.entity.Advert;
import com.group.dto.AdvertDto;

import java.util.List;

public interface AdvertService {
    AdvertDto find(Integer id);

    List<AdvertDto> findAll();

    List<AdvertDto> findByUrl(String url);

    AdvertDto create(AdvertDto advert);

    void delete(Integer id);

    void deleteAll();

    AdvertDto update(AdvertDto advert);

    void update(AdvertDto advert, boolean createOrDelete);
}
