package com.group.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
abstract class CustomConverter<E, D> {
    public List<E> toEntityList(List<D> dtoList) {
        return dtoList.stream().map(this::toEntity).collect(toList());
    }

    public List<D> toDtoList(List<E> entityList) {
        return entityList.stream().map(this::toDto).collect(toList());
    }

    @SuppressWarnings("unchecked")
    public E toEntity(D dto) {
        try {
            E entity = (E) createInstance(0);
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            log.error("Unable to convert dto to entity", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public D toDto(E entity) {
        try {
            D dto = (D) createInstance(1);
            BeanUtils.copyProperties(entity, dto);
            return dto;
        } catch (Exception e) {
            log.error("Unable to convert entity to dto", e);
            throw new RuntimeException(e);
        }
    }

    private Object createInstance(int index) throws IllegalAccessException, InstantiationException {
        ParameterizedType type;
        if (this.getClass().getGenericSuperclass() instanceof ParameterizedType) {
            type = (ParameterizedType) this.getClass().getGenericSuperclass();
        } else {
            type = (ParameterizedType) this.getClass().getSuperclass().getGenericSuperclass();
        }

        return ((Class) type.getActualTypeArguments()[index]).newInstance();
    }
}
