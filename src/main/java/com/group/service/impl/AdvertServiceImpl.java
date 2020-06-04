package com.group.service.impl;

import com.group.converter.AdvertConverter;
import com.group.domain.entity.Advert;
import com.group.dto.AdvertDto;
import com.group.exception.ObjectNotFoundException;
import com.group.repository.AdvertRepository;
import com.group.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvertServiceImpl implements AdvertService {
    private final AdvertRepository repository;
    private final AdvertConverter converter;

    @Autowired
    public AdvertServiceImpl(AdvertRepository repository, AdvertConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    @Override
    public AdvertDto find(Integer id) {
        return repository.findById(id).map(converter::toDto)
                .orElseThrow(() -> new ObjectNotFoundException(AdvertDto.class, id));
    }

    @Override
    public List<AdvertDto> findAll() {
        return converter.toDtoList(repository.findAll());
    }

    @Override
    public List<AdvertDto> findByUrl(String url) {
        return converter.toDtoList(repository.findByUrl(url));
    }

    @Override
    public AdvertDto create(AdvertDto dto) {
        return converter.toDto(repository.save(converter.toEntity(dto)));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public AdvertDto update(AdvertDto dto) {
//        repository.findById(dto.getId())
//                .orElseThrow(() -> new ObjectNotFoundException(AdvertDto.class, dto.getId()));
        return converter.toDto(repository.save(converter.toEntity(dto)));
    }

    @Override
    public void update(AdvertDto dto, boolean createOrDelete) {
        if (createOrDelete) {
            //create(advertDto);
            Advert advert = converter.toEntity(dto);
            repository.insert(advert.getSiteId(), advert.getUrl(), advert.getTitle(), advert.getDescription(),
                    advert.getText(), advert.getPrice(), advert.getDate(), advert.getLocation(), advert.getFromAgent(), advert.isViewed(),
                    advert.isSave(), advert.getGroupName());
        } else {
            findByUrl(dto.getUrl()).forEach(a -> delete(a.getId()));
        }
    }
}
