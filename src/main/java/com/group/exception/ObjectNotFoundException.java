package com.group.exception;

import com.group.dto.AdvertDto;

public class ObjectNotFoundException extends RuntimeException {

    private static final String OBJECT_NOT_FOUND_MESSAGE = "%s with id %d does not exist";
    private static final String OBJECT_NOT_FOUND_MESSAGE_1 = "%s with parameters %d does not exist";

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(Class objectClass, Integer id) {
        this(String.format(OBJECT_NOT_FOUND_MESSAGE, objectClass.getSimpleName(), id));
    }

    public ObjectNotFoundException(Class<AdvertDto> objectClass, String str) {
        this(String.format(OBJECT_NOT_FOUND_MESSAGE, objectClass.getSimpleName(), str));
    }
}

